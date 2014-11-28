//
//  GridView.m
//  instaoverlay
//
//  Created by Saravanan D on 09/10/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import "GridView.h"

@implementation GridView


// -------------------------------------------------------------------------------
// Used for drawing the grids ontop of the view port
// -------------------------------------------------------------------------------
- (void)drawRect:(CGRect)rect
{
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetLineWidth(context, 0.5);
    CGContextSetStrokeColorWithColor(context, [UIColor whiteColor].CGColor);
    
    // ---------------------------
    // Drawing column lines
    // ---------------------------
    
    // calculate column width
    CGFloat columnWidth = self.frame.size.width / (self.numberOfColumns + 1.0);
    
    for(int i = 1; i <= self.numberOfColumns; i++)
    {
        CGPoint startPoint;
        CGPoint endPoint;
        
        startPoint.x = columnWidth * i;
        startPoint.y = 0.0f;
        
        endPoint.x = startPoint.x;
        endPoint.y = self.frame.size.height;
        
        CGContextMoveToPoint(context, startPoint.x, startPoint.y);
        CGContextAddLineToPoint(context, endPoint.x, endPoint.y);
        CGContextStrokePath(context);
    }
    
    // ---------------------------
    // Drawing row lines
    // ---------------------------
    
    // calclulate row height
    CGFloat rowHeight = self.frame.size.height / (self.numberOfRows + 1.0);
    
    for(int j = 1; j <= self.numberOfRows; j++)
    {
        CGPoint startPoint;
        CGPoint endPoint;
        
        startPoint.x = 0.0f;
        startPoint.y = rowHeight * j;
        
        endPoint.x = self.frame.size.width;
        endPoint.y = startPoint.y;
        
        CGContextMoveToPoint(context, startPoint.x, startPoint.y);
        CGContextAddLineToPoint(context, endPoint.x, endPoint.y);
        CGContextStrokePath(context);
    }
}


@end
