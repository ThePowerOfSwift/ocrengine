//
//  MAAppDelegate.h
//  instaoverlay
//
//  Created by Maximilian Mackh on 11/5/12.
//  Copyright (c) 2012 mackh ag. All rights reserved.
//

#import <UIKit/UIKit.h>

#define IS_CAPTURED_MANUALLY    NO

#define SERVER_URL      @"http://172.17.1.215:4565/process"


#define SCREEN_WIDTH      [[UIScreen mainScreen] bounds].size.width
#define SCREEN_HEIGHT      [[UIScreen mainScreen] bounds].size.height

@class MAViewController;

@interface MAAppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (strong, nonatomic) MAViewController *viewController;

@property (assign,nonatomic)  BOOL isProcessing;
@property (assign,nonatomic)  BOOL isUploading;
@property (assign,nonatomic)  CGRect overlayRectangle;

@end
