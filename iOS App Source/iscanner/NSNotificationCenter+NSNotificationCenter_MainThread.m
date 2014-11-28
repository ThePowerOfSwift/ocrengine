//
//  NSNotificationCenter+NSNotificationCenter_MainThread.m
//  instaoverlay
//
//  Created by Saravanan D on 08/10/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import "NSNotificationCenter+NSNotificationCenter_MainThread.h"

@implementation NSNotificationCenter (NSNotificationCenter_MainThread)

- (void)postNotificationOnMainThread:(NSNotification *)notification
{
    [self performSelectorOnMainThread:@selector(postNotification:) withObject:notification waitUntilDone:YES];
}

- (void)postNotificationOnMainThreadName:(NSString *)aName object:(id)anObject
{
    NSNotification *notification = [NSNotification notificationWithName:aName object:anObject];
    [self postNotificationOnMainThread:notification];
}

- (void)postNotificationOnMainThreadName:(NSString *)aName object:(id)anObject userInfo:(NSDictionary *)aUserInfo
{
    NSNotification *notification = [NSNotification notificationWithName:aName object:anObject userInfo:aUserInfo];
    [self postNotificationOnMainThread:notification];
}

@end
