//
//  NSNotificationCenter+NSNotificationCenter_MainThread.h
//  instaoverlay
//
//  Created by Saravanan D on 08/10/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSNotificationCenter (NSNotificationCenter_MainThread)

- (void)postNotificationOnMainThread:(NSNotification *)notification;
- (void)postNotificationOnMainThreadName:(NSString *)aName object:(id)anObject;
- (void)postNotificationOnMainThreadName:(NSString *)aName object:(id)anObject userInfo:(NSDictionary *)aUserInfo;

@end
