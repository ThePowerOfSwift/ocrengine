//
//  MACaptureSession.m
//  instaoverlay
//
//  Created by Maximilian Mackh on 11/5/12.
//  Copyright (c) 2012 mackh ag. All rights reserved.
//

#import "MACaptureSession.h"
#import <ImageIO/ImageIO.h>
#import "ProcessCapturedImages.h"
#import "MAAppDelegate.h"
#import "MAConstants.h"


@implementation MACaptureSession

@synthesize captureSession = _captureSession;
@synthesize previewLayer = _previewLayer;
@synthesize stillImageOutput = _stillImageOutput;
@synthesize stillImage = _stillImage;

- (id)init
{
	if ((self = [super init]))
    {
		[self setCaptureSession:[[AVCaptureSession alloc] init]];
        NSLog(@"MACaptureSession : init");
        isLightTurnedOn=NO;
	}
	return self;
}

- (void)addVideoPreviewLayer
{
	[self setPreviewLayer:[[AVCaptureVideoPreviewLayer alloc] initWithSession:[self captureSession]]];
	[_previewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
}

- (void)addVideoInputFromCamera
{
    AVCaptureDevice *backCamera;
    
    NSArray *devices = [AVCaptureDevice devices];
    
    for (AVCaptureDevice *device in devices)
    {
        if ([device hasMediaType:AVMediaTypeVideo])
        {
            if ([device position] == AVCaptureDevicePositionBack)
            {
                backCamera = device;
                [self toggleFlash];
            }
        }
    }
    
    NSError *error = nil;
    
    AVCaptureDeviceInput *backFacingCameraDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:backCamera error:&error];
    
    if (!error)
    {
        if ([_captureSession canAddInput:backFacingCameraDeviceInput])
        {
            [_captureSession addInput:backFacingCameraDeviceInput];
        }
    }
    
    //SARAVANAN, ADD VIDEO OUTPUT
    
    // Create a VideoDataOutput and add it to the session
    AVCaptureVideoDataOutput *output = [[AVCaptureVideoDataOutput alloc] init];
    [_captureSession addOutput:output];
    
    // Configure your output.
    dispatch_queue_t queue = dispatch_queue_create("myQueue", NULL);
    [output setSampleBufferDelegate:self queue:queue];
    
    // Specify the pixel format
    output.videoSettings = [NSDictionary dictionaryWithObject:
                                [NSNumber numberWithInt:kCVPixelFormatType_32BGRA]
                                                forKey:(id)kCVPixelBufferPixelFormatTypeKey];
//    [NSDictionary dictionaryWithObject:[NSNumber numberWithUnsignedInt:kCVPixelFormatType_420YpCbCr8BiPlanarFullRange] forKey:(NSString *)kCVPixelBufferPixelFormatTypeKey];
}

- (void)setFlashOn:(BOOL)boolWantsFlash
{
    flashOn = boolWantsFlash;
    [self toggleFlash];
}

- (void)toggleFlash
{
    NSArray *devices = [AVCaptureDevice devices];
    
    for (AVCaptureDevice *device in devices)
    {
        if (device.flashAvailable) {
            if (flashOn)
            {
                [device lockForConfiguration:nil];
                device.flashMode = AVCaptureFlashModeOn;
                [device unlockForConfiguration];
            }
            else
            {
                [device lockForConfiguration:nil];
                device.flashMode = AVCaptureFlashModeOff;
                [device unlockForConfiguration];
            }
        }
    }
}

- (void)addStillImageOutput
{
    [self setStillImageOutput:[[AVCaptureStillImageOutput alloc] init]];
    NSDictionary *outputSettings = [[NSDictionary alloc] initWithObjectsAndKeys:AVVideoCodecJPEG,AVVideoCodecKey,nil];
    [[self stillImageOutput] setOutputSettings:outputSettings];
    
    AVCaptureConnection *videoConnection = nil;
    
    for (AVCaptureConnection *connection in [_stillImageOutput connections])
    {
        for (AVCaptureInputPort *port in [connection inputPorts])
        {
            if ([[port mediaType] isEqual:AVMediaTypeVideo] )
            {
                videoConnection = connection;
                break;
            }
        }
        if (videoConnection)
        {
            break;
        }
    }
    [_captureSession setSessionPreset:AVCaptureSessionPresetPhoto];
    [_captureSession addOutput:[self stillImageOutput]];
}

- (void)captureStillImage
{
	AVCaptureConnection *videoConnection = nil;
    NSLog(@"captureStill : %@",[self stillImageOutput]);
	for (AVCaptureConnection *connection in [[self stillImageOutput] connections])
    {
		for (AVCaptureInputPort *port in [connection inputPorts])
        {
			if ([[port mediaType] isEqual:AVMediaTypeVideo])
            {
                NSLog(@"captureStill - Video");
				videoConnection = connection;
				break;
			}
		}
        
		if (videoConnection)
        {
            break;
        }
	}
    
	[_stillImageOutput captureStillImageAsynchronouslyFromConnection:videoConnection
                                                         completionHandler:
     ^(CMSampleBufferRef imageSampleBuffer, NSError *error) {
         
         if (imageSampleBuffer)
         {
             CFDictionaryRef exifAttachments = CMGetAttachment(imageSampleBuffer, kCGImagePropertyExifDictionary, NULL);
             if (exifAttachments)
             {
                 //NSLog(@"attachements: %@", exifAttachments);
             } else
             {
                 //NSLog(@"no attachments");
             }
             NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageSampleBuffer];
             UIImage *image = [[UIImage alloc] initWithData:imageData];
             
             [self setStillImage:[self getCroppedImage:image]];
             [[NSNotificationCenter defaultCenter] postNotificationName:kImageCapturedSuccessfully object:nil];
         }
     }];
}

-(UIImage*) getCroppedImage:(UIImage*) image {
    //Overlay
    int yPosition = 0;
//    if(SYSTEM_VERSION_LESS_THAN(@"8"))
//    {
//        yPosition=(SCREEN_WIDTH -kCameraToolBarHeight)-200;
//    }
//    else{
        yPosition=(SCREEN_HEIGHT -kCameraToolBarHeight)-200;
//    }
    // (50, yPosition/2-20, 350, 200)
    CGRect overlayRect=CGRectMake(40, yPosition/2-20, [[UIScreen mainScreen] bounds].size.width-80, 230);
    
    float left = (overlayRect.origin.x*image.size.width)/SCREEN_WIDTH;
    float top  = (overlayRect.origin.y*image.size.height)/(SCREEN_HEIGHT -kCameraToolBarHeight);
    float width = ((overlayRect.size.width)*image.size.width)/SCREEN_WIDTH;
    float height = ((overlayRect.size.height)*image.size.height)/(SCREEN_HEIGHT -kCameraToolBarHeight);
    
    UIImage *squareImage = [self croppedImageInRect:CGRectMake(top, left, width, height) IMAGE:image];
    
    NSLog(@"Original image size = %@",NSStringFromCGSize(image.size));
    NSLog(@"Cropped Image size : %@",NSStringFromCGSize(squareImage.size));
    
    return squareImage;
}

// Delegate routine that is called when a sample buffer was written
- (void)captureOutput:(AVCaptureOutput *)captureOutput
didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
       fromConnection:(AVCaptureConnection *)connection
{
    if (IS_CAPTURED_MANUALLY) {
        return;
    }
    
    CFDictionaryRef metadataDict = CMCopyDictionaryOfAttachments(NULL, sampleBuffer, kCMAttachmentMode_ShouldPropagate);
    NSDictionary *metadata = [[NSMutableDictionary alloc]  initWithDictionary:(__bridge NSDictionary*)metadataDict];
    CFRelease(metadataDict);
    NSDictionary *exifMetadata = [[metadata objectForKey:(NSString *)kCGImagePropertyExifDictionary] mutableCopy];
    float brightnessValue = [[exifMetadata  objectForKey:(NSString *)kCGImagePropertyExifBrightnessValue] floatValue];
    
    //NSLog(@"brightnessValue = %f",brightnessValue);
    
    if (!isLightTurnedOn && brightnessValue<1.5) {
        isLightTurnedOn=YES;
        AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        if ([device hasTorch]) {
            [device lockForConfiguration:nil];
            [device setTorchMode:AVCaptureTorchModeOn];  // use AVCaptureTorchModeOff to turn off
            [device unlockForConfiguration];
        }
    }
    
    MAAppDelegate *delegate=(MAAppDelegate*)[[UIApplication sharedApplication]delegate];
    
    if (!delegate.isProcessing) {
        delegate.isProcessing=YES;
        
        UIImage *image = [self imageFromSampleBuffer:sampleBuffer];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            //< Add your code here that uses the image >
            NSLog(@"Image = %@",image);
            
            [self setStillImage:[self getCroppedImage:image]];
            [[ProcessCapturedImages sharedInstance]detectEdges:self.stillImage];
        });
    }
}


// Create a UIImage from sample buffer data
- (UIImage *) imageFromSampleBuffer:(CMSampleBufferRef) sampleBuffer
{
    // Get a CMSampleBuffer's Core Video image buffer for the media data
    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    // Lock the base address of the pixel buffer
    CVPixelBufferLockBaseAddress(imageBuffer, 0);
    
    // Get the number of bytes per row for the pixel buffer
    void *baseAddress = CVPixelBufferGetBaseAddress(imageBuffer);
    
    // Get the number of bytes per row for the pixel buffer
    size_t bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
    // Get the pixel buffer width and height
    size_t width = CVPixelBufferGetWidth(imageBuffer);
    size_t height = CVPixelBufferGetHeight(imageBuffer);
    
    // Create a device-dependent RGB color space
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    
    // Create a bitmap graphics context with the sample buffer data
    CGContextRef context = CGBitmapContextCreate(baseAddress, width, height, 8,
                                                 bytesPerRow, colorSpace, kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedFirst);
    // Create a Quartz image from the pixel data in the bitmap graphics context
    CGImageRef quartzImage = CGBitmapContextCreateImage(context);
    // Unlock the pixel buffer
    CVPixelBufferUnlockBaseAddress(imageBuffer,0);
    
    
    // Free up the context and color space
    CGContextRelease(context);
    CGColorSpaceRelease(colorSpace);
    
    //    // Create an image object from the Quartz image
    //    UIImage *image = [UIImage imageWithCGImage:quartzImage];
    
    // Create an image object from the Quartz image
    //I modified this line: [UIImage imageWithCGImage:quartzImage]; to the following to correct the orientation:
    UIImage *image =  [UIImage imageWithCGImage:quartzImage scale:1.0 orientation:UIImageOrientationRight];
    
    // Release the Quartz image
    CGImageRelease(quartzImage);
    
    return (image);
}


- (void)fitImageView:(UIImage*) image CONTAINER:(UIImageView*) tContainer
{
    float x,y;
    float a,b;
    x = tContainer.frame.size.width;
    y = tContainer.frame.size.height;
    a = image.size.width;
    b = image.size.height;
    
    if (a > x || b > y) {
        tContainer.contentMode = UIViewContentModeScaleAspectFit;
    } else {
        tContainer.contentMode = UIViewContentModeCenter;
    }
    tContainer.image = image;
}

- (UIImage *)croppedImageInRect:(CGRect)rect IMAGE:(UIImage*) image
{
    double (^rad)(double) = ^(double deg) {
        return deg / 180.0 * M_PI;
    };
    
    CGAffineTransform rectTransform;
    switch (image.imageOrientation) {
        case UIImageOrientationLeft:
            rectTransform = CGAffineTransformTranslate(CGAffineTransformMakeRotation(rad(90)), 0, -image.size.height);
            break;
        case UIImageOrientationRight:
            rectTransform = CGAffineTransformTranslate(CGAffineTransformMakeRotation(rad(-90)), -image.size.width, 0);
            break;
        case UIImageOrientationDown:
            rectTransform = CGAffineTransformTranslate(CGAffineTransformMakeRotation(rad(-180)), -image.size.width, -image.size.height);
            break;
        default:
            rectTransform = CGAffineTransformIdentity;
    };
    rectTransform = CGAffineTransformScale(rectTransform, image.scale, image.scale);
    
    CGImageRef imageRef = CGImageCreateWithImageInRect([image CGImage], CGRectApplyAffineTransform(rect, rectTransform));
    UIImage *result = [UIImage imageWithCGImage:imageRef scale:image.scale orientation:image.imageOrientation];
    CGImageRelease(imageRef);
    
    return result;
}

- (void)dealloc {
    NSLog(@"MACaptureSession : releasing notification");
//    [[NSNotificationCenter defaultCenter] removeObserver:kResetFrameCapture];
	//[[self captureSession] stopRunning];
    
	_previewLayer = nil;
	_captureSession = nil;
    _stillImageOutput = nil;
    _stillImage = nil;
}



@end
