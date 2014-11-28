//
//  GridView.h
//  instaoverlay
//
//  Created by Saravanan D on 09/10/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GridView : UIView

@property (nonatomic, assign) int numberOfColumns;
@property (nonatomic, assign) int numberOfRows;

- (void)drawRect:(CGRect)rect;

@end