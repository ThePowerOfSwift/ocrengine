//
//  MAInformationViewController.m
//  instaoverlay
//
//  Created by Suryakant Sharma on 11/7/14.
//  Copyright (c) 2014 mackh ag. All rights reserved.
//

#import "MAInformationViewController.h"

@interface MAInformationViewController ()

@end

@implementation MAInformationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    NSMutableString *str = [[NSMutableString alloc] init];
    for(NSString *aKey in _infoDict.allKeys){
        NSCharacterSet *notAllowedChars = [[NSCharacterSet characterSetWithCharactersInString:@"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"] invertedSet];
        NSString *resultString = [[aKey componentsSeparatedByCharactersInSet:notAllowedChars] componentsJoinedByString:@""];
        NSLog (@"Result: %@", resultString);
        [str appendString:resultString];
        [str appendString:@"\t:"];
        int value = [[[_infoDict objectForKey:aKey] objectAtIndex:0]floatValue];
        [str appendFormat:@"%d",value];
        [str appendString:@"\n"];
    }
    _testView.text = str;
}

-(IBAction)doneButton:(UIButton *)sender{
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
