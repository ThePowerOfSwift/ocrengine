//
//  ProcessCapturedImages.h
//  instaoverlay
//
//  Created by Saravanan D on 06/10/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ProcessCapturedImages : NSObject


+ (id)sharedInstance;
- (void)detectEdges:(UIImage*) image;

-(void) checkForBurryImage:(UIImage *) image;

@end
