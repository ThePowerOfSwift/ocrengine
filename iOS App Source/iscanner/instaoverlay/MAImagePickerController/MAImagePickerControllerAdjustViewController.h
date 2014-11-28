//
//  MAImagePickerControllerAdjustViewController.h
//  instaoverlay
//
//  Created by Maximilian Mackh on 11/5/12.
//  Copyright (c) 2012 mackh ag. All rights reserved.
//

//Dedected Points

#import <UIKit/UIKit.h>

#import "MAConstants.h"
#import "MADrawRect.h"

#import <opencv2/core/core.hpp>
#import <opencv2/imgproc/imgproc.hpp>
#import "MAImagePickerFinalViewController.h"

@interface MAImagePickerControllerAdjustViewController : NSObject
{
    BOOL isGray;
}

@property (strong, nonatomic) UIImageView *sourceImageView;
@property (strong, nonatomic) UIToolbar *adjustToolBar;
@property (strong, nonatomic) UIImage *sourceImage;
@property (strong, nonatomic) UIImage *adjustedImage;
@property (strong, nonatomic) MADrawRect *adjustRect;
@property (strong, nonatomic)  MAImagePickerFinalViewController *finalView;

@property (strong, nonatomic) UIView *imageAdjustView;

- (void)setUpAdjustImageView;

@end
