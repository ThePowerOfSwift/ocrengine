//
//  ProcessCapturedImages.m
//  instaoverlay
//
//  Created by Saravanan D on 06/10/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import "ProcessCapturedImages.h"


#import "UIImageView+ContentFrame.h"
#import <QuartzCore/QuartzCore.h>

#import <opencv2/core/core.hpp>
#import <opencv2/imgproc/imgproc.hpp>

#import "MAOpenCV.h"

#import "MAConstants.h"
#import "MADrawRect.h"
#import "NSNotificationCenter+NSNotificationCenter_MainThread.h"


@implementation ProcessCapturedImages


+ (id)sharedInstance {
    static ProcessCapturedImages *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (void)detectEdges:(UIImage*) image
{
    NSLog(@"ProcessCapturedImages : detectEdges");
    [self isBlurry:image];
    
    cv::Mat original = [MAOpenCV cvMatFromUIImage:image];
    CGSize targetSize = CGSizeMake(320, 568 - 54);//_sourceImageView.contentSize;
    cv::resize(original, original, cvSize(targetSize.width, targetSize.height));
    
    cv::vector<cv::vector<cv::Point>>squares;
    cv::vector<cv::Point> largest_square;
    
    find_squares1(original, squares);
    find_largest_square1(squares, largest_square);
    
    NSLog(@"squares : size = %lu",squares.size());
    NSLog(@"largestSquare : size = %lu",largest_square.size());
    
    if (largest_square.size() == 4)
    {
        NSLog(@"IMAGE CAPTURED SUCCESSFULLY...");
        // Manually sorting points, needs major improvement. Sorry.
        
        NSMutableArray *points = [NSMutableArray array];
        NSMutableDictionary *sortedPoints = [NSMutableDictionary dictionary];
        
        for (int i = 0; i < 4; i++)
        {
            NSDictionary *dict = [NSDictionary dictionaryWithObjectsAndKeys:[NSValue valueWithCGPoint:CGPointMake(largest_square[i].x, largest_square[i].y)], @"point" , [NSNumber numberWithInt:(largest_square[i].x + largest_square[i].y)], @"value", nil];
            [points addObject:dict];
        }
        
        int min = [[points valueForKeyPath:@"@min.value"] intValue];
        int max = [[points valueForKeyPath:@"@max.value"] intValue];
        
        int minIndex;
        int maxIndex;
        
        int missingIndexOne;
        int missingIndexTwo;
        
        for (int i = 0; i < 4; i++)
        {
            NSDictionary *dict = [points objectAtIndex:i];
            
            if ([[dict objectForKey:@"value"] intValue] == min)
            {
                [sortedPoints setObject:[dict objectForKey:@"point"] forKey:@"0"];
                minIndex = i;
                continue;
            }
            
            if ([[dict objectForKey:@"value"] intValue] == max)
            {
                [sortedPoints setObject:[dict objectForKey:@"point"] forKey:@"2"];
                maxIndex = i;
                continue;
            }
            
            NSLog(@"MSSSING %i", i);
            
            missingIndexOne = i;
        }
        
        for (int i = 0; i < 4; i++)
        {
            if (missingIndexOne != i && minIndex != i && maxIndex != i)
            {
                missingIndexTwo = i;
            }
        }
        
        
        if (largest_square[missingIndexOne].x < largest_square[missingIndexTwo].x)
        {
            NSLog(@"2nd Point Found");
            //2nd Point Found
            [sortedPoints setObject:[[points objectAtIndex:missingIndexOne] objectForKey:@"point"] forKey:@"3"];
            [sortedPoints setObject:[[points objectAtIndex:missingIndexTwo] objectForKey:@"point"] forKey:@"1"];
        }
        else
        {
            NSLog(@"4rd Point Found");
            //4rd Point Found
            [sortedPoints setObject:[[points objectAtIndex:missingIndexOne] objectForKey:@"point"] forKey:@"1"];
            [sortedPoints setObject:[[points objectAtIndex:missingIndexTwo] objectForKey:@"point"] forKey:@"3"];
        }
        
    }
    
    original.release();
    
     NSLog(@"final largeSquare : size = %lu",largest_square.size());
    // Check if the captured frame contains square/rectangle
    NSDictionary *dic;
    if (largest_square.size()>1) {
        dic=[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"result"];
    }else {
        dic=[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"result"];
    }
    NSLog(@"dic  = %@",dic);
    
    [[NSNotificationCenter defaultCenter] postNotificationOnMainThreadName:kResetFrameCapture object:nil userInfo:dic];
}


void find_squares1(cv::Mat& image, cv::vector<cv::vector<cv::Point>>&squares) {
    
    // blur will enhance edge detection
    cv::Mat blurred(image);
    medianBlur(image, blurred, 9);
    
    cv::Mat gray0(blurred.size(), CV_8U), gray;
    cv::vector<cv::vector<cv::Point> > contours;
    
    // find squares in every color plane of the image
    for (int c = 0; c < 3; c++)
    {
        int ch[] = {c, 0};
        mixChannels(&blurred, 1, &gray0, 1, ch, 1);
        
        // try several threshold levels
        const int threshold_level = 2;
        for (int l = 0; l < threshold_level; l++)
        {
            // Use Canny instead of zero threshold level!
            // Canny helps to catch squares with gradient shading
            if (l == 0)
            {
                Canny(gray0, gray, 10, 20, 3); //
                
                // Dilate helps to remove potential holes between edge segments
                dilate(gray, gray, cv::Mat(), cv::Point(-1,-1));
            }
            else
            {
                gray = gray0 >= (l+1) * 255 / threshold_level;
            }
            
            // Find contours and store them in a list
            findContours(gray, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
            
            // Test contours
            cv::vector<cv::Point> approx;
            for (size_t i = 0; i < contours.size(); i++)
            {
                // approximate contour with accuracy proportional
                // to the contour perimeter
                approxPolyDP(cv::Mat(contours[i]), approx, arcLength(cv::Mat(contours[i]), true)*0.02, true);
                
                // Note: absolute value of an area is used because
                // area may be positive or negative - in accordance with the
                // contour orientation
                if (approx.size() == 4 &&
                    fabs(contourArea(cv::Mat(approx))) > 1000 &&
                    isContourConvex(cv::Mat(approx)))
                {
                    double maxCosine = 0;
                    
                    for (int j = 2; j < 5; j++)
                    {
                        double cosine = fabs(angle1(approx[j%4], approx[j-2], approx[j-1]));
                        maxCosine = MAX(maxCosine, cosine);
                    }
                    
                    if (maxCosine < 0.3)
                        squares.push_back(approx);
                }
            }
        }
    }
}

void find_largest_square1(const cv::vector<cv::vector<cv::Point> >& squares, cv::vector<cv::Point>& biggest_square)
{
    NSLog(@"find_largest_square1 : size = %lu",squares.size());
    if (!squares.size())
    {
        // no squares detected
        return;
    }
    
    int max_width = 0;
    int max_height = 0;
    int max_square_idx = 0;
    
    for (size_t i = 0; i < squares.size(); i++)
    {
        // Convert a set of 4 unordered Points into a meaningful cv::Rect structure.
        cv::Rect rectangle = boundingRect(cv::Mat(squares[i]));
        
        //        cout << "find_largest_square: #" << i << " rectangle x:" << rectangle.x << " y:" << rectangle.y << " " << rectangle.width << "x" << rectangle.height << endl;
        
        // Store the index position of the biggest square found
        if ((rectangle.width >= max_width) && (rectangle.height >= max_height))
        {
            max_width = rectangle.width;
            max_height = rectangle.height;
            max_square_idx = i;
        }
        NSLog(@"%zu square : width = %d height = %d",i,max_width,max_height);
    }
     NSLog(@"Width : %d Height : %d",max_width,max_height);

//    if (max_width>230 && max_height>180)
    //if (max_width>200 && max_height>350)
    if (max_width>210 && max_height>380)
    {
        biggest_square = squares[max_square_idx];
    }
    
    NSLog(@"Square size is lesser than expected...");
    return;
}


double angle1( cv::Point pt1, cv::Point pt2, cv::Point pt0 ) {
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}

cv::Mat debugSquares1( std::vector<std::vector<cv::Point> > squares, cv::Mat image ){
    
    NSLog(@"DEBUG!/?!");
    for ( unsigned int i = 0; i< squares.size(); i++ ) {
        // draw contour
        
        NSLog(@"LOOP!");
        
        cv::drawContours(image, squares, i, cv::Scalar(255,0,0), 1, 8, std::vector<cv::Vec4i>(), 0, cv::Point());
        
        // draw bounding rect
        cv::Rect rect = boundingRect(cv::Mat(squares[i]));
        cv::rectangle(image, rect.tl(), rect.br(), cv::Scalar(0,255,0), 2, 8, 0);
        
        // draw rotated rect
        cv::RotatedRect minRect = minAreaRect(cv::Mat(squares[i]));
        cv::Point2f rect_points[4];
        minRect.points( rect_points );
        for ( int j = 0; j < 4; j++ ) {
            cv::line( image, rect_points[j], rect_points[(j+1)%4], cv::Scalar(0,0,255), 1, 8 ); // blue
        }
    }
    
    return image;
}

-(void) isBlurry:(UIImage *) image {
    cv::Mat matImage = [MAOpenCV cvMatFromUIImage:image];
    std::cout<<"Number of pixels : matImage = " << matImage.total() << std::endl;
    
    cv::Mat edges;
    Canny(matImage, edges, 225, 175, 3);
    
    std::cout<<"Number of pixels : edges = " << edges.total() << std::endl;
    
}

-(void) checkForBurryImage:(UIImage *) image {
    NSLog(@"MAImagePicker : checkForBurryImage");
    
    cv::Mat matImage = [MAOpenCV cvMatFromUIImage:image];
    cv::Mat matImageGrey;
    cv::cvtColor(matImage, matImageGrey, CV_BGRA2GRAY);
    
    cv::Mat dst2 =[MAOpenCV cvMatFromUIImage:image];
    cv::Mat laplacianImage;
    dst2.convertTo(laplacianImage, CV_8UC1);
    cv::Laplacian(matImageGrey, laplacianImage, CV_8U);
    cv::Mat laplacianImage8bit;
    laplacianImage.convertTo(laplacianImage8bit, CV_8UC1);
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    unsigned char *pixels = laplacianImage8bit.data;
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    //    unsigned char *pixels = laplacianImage8bit.data;
    int maxLap = -16777216;
    
    for (int i = 0; i < ( laplacianImage8bit.elemSize()*laplacianImage8bit.total()); i++) {
        if (pixels[i] > maxLap)
            maxLap = pixels[i];
    }
    
    int soglia = -6118750;
    
    printf("\n maxLap : %i",maxLap);
    
    
    if (maxLap < soglia || maxLap == soglia) {
        printf("\n\n***** blur image *****");
    }else
        printf("\nNOT a blur image");
}

/*
-(void) isBlur {
    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inDither = true;
    opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap image = BitmapFactory.decodeByteArray(im, 0, im.length);
    int l = CvType.CV_8UC1; //8-bit grey scale image
    Mat matImage = new Mat();
    Utils.bitmapToMat(image, matImage);
    Mat matImageGrey = new Mat();
    Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);
    
    Bitmap destImage;
    destImage = Bitmap.createBitmap(image);
    Mat dst2 = new Mat();
    Utils.bitmapToMat(destImage, dst2);
    Mat laplacianImage = new Mat();
    dst2.convertTo(laplacianImage, l);
    Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
    Mat laplacianImage8bit = new Mat();
    laplacianImage.convertTo(laplacianImage8bit, l);
    
    Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(),
                                     laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
    Utils.matToBitmap(laplacianImage8bit, bmp);
    int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
    bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                  bmp.getHeight());
    
    int maxLap = -16777216;
    
    for (int i = 0; i < pixels.length; i++) {
        if (pixels[i] > maxLap)
            maxLap = pixels[i];
    }
    
    int soglia = -6118750;      
    
    if (maxLap < soglia || maxLap == soglia) {
        Log.d(MIOTAG, "blur image");
    }

}
*/

















@end
