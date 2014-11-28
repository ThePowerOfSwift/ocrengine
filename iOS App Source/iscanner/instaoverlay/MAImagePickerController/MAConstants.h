//
//  MAConstants.h
//  instaoverlay
//
//  Created by Maximilian Mackh on 11/6/12.
//  Copyright (c) 2012 mackh ag. All rights reserved.
//

#import <Foundation/Foundation.h>

#define kImageCapturedSuccessfully @"imageCapturedSuccessfully"
#define kCameraToolBarHeight 54
#define kCameraFlashDefaultsKey @"MAImagePickerControllerFlashIsOn"
#define kCropButtonSize 200
#define kActivityIndicatorSize 100

#define kIMAGE_OVERLAY_WIDTH 
#define kIMAGE_OVERLAY_HEIGHT

#define SYSTEM_VERSION_LESS_THAN(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] == NSOrderedAscending)



#define kResetFrameCapture @"resetFrameCapture"

#define kIMAGE_UPLOAD_API @"http://172.17.1.215:4565/process"


@interface MAConstants : NSObject

@end
