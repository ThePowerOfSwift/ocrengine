//
//  MAViewController.m
//  instaoverlay
//
//  Created by Maximilian Mackh on 11/5/12.
//  Copyright (c) 2012 mackh ag. All rights reserved.
//

#import "MAViewController.h"
#import "AFNetworking.h"
#import "MAAppDelegate.h"


#define DEST_PATH               [NSHomeDirectory() stringByAppendingString:@"/Documents/"]

@interface MAViewController ()
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *spinner;
@property (weak, nonatomic) IBOutlet UILabel *uploadingLabel;
@property (weak, nonatomic) IBOutlet UIButton *startCamera;
@property (weak, nonatomic) IBOutlet UIButton *updateUrl;

@end

@implementation MAViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    NSLog(@"main screen bound = %@",NSStringFromCGRect([[UIScreen mainScreen] bounds]));
    NSLog(@"View frame = %@",NSStringFromCGRect(self.view.frame));
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(stopSpinner:) name:@"stop_spinner" object:nil];
    
    NSLog(@"serverUrl : %@",[[NSUserDefaults standardUserDefaults]stringForKey:@"serverUrl"]);
}

-(void) stopSpinner:(NSNotification*) notifi {
    NSDictionary *dic=[notifi userInfo];
    NSLog(@"dic = %@",dic);
    
//    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:[dic objectForKey:@"statusCode"]
//                                                    message:[dic objectForKey:@"responseString"]
//                                                   delegate:nil
//                                          cancelButtonTitle:@"OK"
//                                          otherButtonTitles:nil];
//    [alert show];

    [_spinner stopAnimating];
    _uploadingLabel.hidden=YES;
    _startCamera.hidden=NO;
    _updateUrl.hidden=NO;
}

- (IBAction)updateURL:(id)sender {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:@"Enter the url to update"
                                                   delegate:self
                                          cancelButtonTitle:@"Done"
                                          otherButtonTitles:nil];
    alert.alertViewStyle = UIAlertViewStylePlainTextInput;
    [alert show];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    NSString *inputString=[alertView textFieldAtIndex:0].text;
    NSLog(@"clickedButtonAtIndex : %@", inputString);
    if (inputString.length>5) {
        [[NSUserDefaults standardUserDefaults]setObject:inputString forKey:@"serverUrl"];
        [_updateUrl setTitle:[[NSUserDefaults standardUserDefaults]stringForKey:@"serverUrl"] forState:UIControlStateNormal];
    }
}

-(void) viewDidAppear:(BOOL)animated {
    
//    MAImagePickerController *imagePicker = [[MAImagePickerController alloc] init];
//
//    [imagePicker setDelegate:self];
//    [imagePicker setSourceType:MAImagePickerControllerSourceTypeCamera];
//
//    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:imagePicker];
//    
//    [self presentViewController:navigationController animated:YES completion:nil];
    
    if (![[NSUserDefaults standardUserDefaults]stringForKey:@"serverUrl"].length) [[NSUserDefaults standardUserDefaults]setObject:SERVER_URL  forKey:@"serverUrl"];
    
    
    [_updateUrl setTitle:[[NSUserDefaults standardUserDefaults]stringForKey:@"serverUrl"] forState:UIControlStateNormal];
    
    MAAppDelegate *appDelegate=(MAAppDelegate*)[[UIApplication sharedApplication]delegate];
    
    if (appDelegate.isUploading) {
        [_spinner startAnimating];
        _uploadingLabel.hidden=NO;
        _startCamera.hidden=YES;
        _updateUrl.hidden=YES;
    }else {
        [_spinner stopAnimating];
        _uploadingLabel.hidden=YES;
        _startCamera.hidden=NO;
        _updateUrl.hidden=NO;
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)initButton:(id)sender
{
    MAImagePickerController *imagePicker = [[MAImagePickerController alloc] init];
   
    [imagePicker setDelegate:self];
    [imagePicker setSourceType:MAImagePickerControllerSourceTypeCamera];
    
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:imagePicker];
    
    [self presentViewController:navigationController animated:YES completion:nil];
}

- (void)imagePickerDidCancel
{
    NSLog(@"imagePickerDidCancel");
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)imagePickerDidChooseImageWithPath:(NSString *)path
{
    NSLog(@"imagePickerDidChooseImageWithPath");
    
    // do not need to dissmiss view controller.
    //[self dismissViewControllerAnimated:YES completion:nil];
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:path])
    {
        NSLog(@"File Found at %@", path);
       
        
    }
    else
    {
        NSLog(@"No File Found at %@", path);
    }
    
    [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
}

@end
