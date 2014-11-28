//
//  MAInformationViewController.h
//  instaoverlay
//
//  Created by Suryakant Sharma on 11/7/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MAInformationViewController : UIViewController

@property(nonatomic,assign) IBOutlet UITextView *testView;
@property(nonatomic,assign) IBOutlet UIButton   *done;
@property(nonatomic,strong) NSDictionary *infoDict;


-(IBAction)doneButton:(UIButton *)sender;

@end
