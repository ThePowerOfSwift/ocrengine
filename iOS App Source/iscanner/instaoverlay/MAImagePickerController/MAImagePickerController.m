//
//  MAImagePickerController.m
//  instaoverlay
//
//  Created by Maximilian Mackh on 11/5/12.
//  Copyright (c) 2012 mackh ag. All rights reserved.
//

#import "MAImagePickerController.h"
#import "MAImagePickerControllerAdjustViewController.h"

#import "UIImage+fixOrientation.h"
#import "MAAppDelegate.h"
#import "GridView.h"
#import "MAInformationViewController.h"


@interface MAImagePickerController ()
- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo;
@end

@implementation MAImagePickerController
{
    BOOL volumeChangeOK;
}

@synthesize captureManager = _captureManager;
@synthesize cameraToolbar = _cameraToolbar;
@synthesize flashButton = _flashButton;
@synthesize pictureButton = _pictureButton;
@synthesize cameraPictureTakenFlash = _cameraPictureTakenFlash;

@synthesize invokeCamera = _invokeCamera;
@synthesize fillLayer=_fillLayer;

- (void)viewDidLoad
{
    [self.navigationController setNavigationBarHidden:YES];
    [self.view setBackgroundColor:[UIColor blackColor]];
    
//    [[NSNotificationCenter defaultCenter] addObserver:self
//                                             selector:@selector(orientationChanged)
//                                                 name:UIDeviceOrientationDidChangeNotification
//                                               object:nil];
    
    
    
    if (_sourceType == MAImagePickerControllerSourceTypeCamera && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(imageAccepted:) name:@"MAImageAccepted" object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(imageUploadingStart) name:@"uploadingStart" object:nil];

        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(MAImagePickerChosen:) name:@"MAIPCSuccessInternal" object:nil];
        
        
        
        [[NSNotificationCenter defaultCenter] addObserverForName:UIApplicationWillEnterForegroundNotification object:nil queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification *notification){
            AudioSessionInitialize(NULL, NULL, NULL, NULL);
            AudioSessionSetActive(YES);
        }];
        
        [[NSNotificationCenter defaultCenter] addObserverForName:UIApplicationDidEnterBackgroundNotification object:nil queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification *notification)
         {
             AudioSessionSetActive(NO);
         }];
        
        
        AudioSessionInitialize(NULL, NULL, NULL, NULL);
        AudioSessionSetActive(YES);
        
        // Volume View to hide System HUD
        _volumeView = [[MPVolumeView alloc] initWithFrame:CGRectMake(-100, 0, 10, 0)];
        [_volumeView sizeToFit];
        [self.view addSubview:_volumeView];
        
        
        
        
        [self setCaptureManager:[[MACaptureSession alloc] init]];
        [_captureManager addVideoInputFromCamera];
        [_captureManager addStillImageOutput];
        [_captureManager addVideoPreviewLayer];
        [[_captureManager previewLayer] setOrientation:AVCaptureVideoOrientationLandscapeRight];

        
        // change here
        CGRect layerRect = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height - kCameraToolBarHeight);
        
        //Since bounds
        if(SYSTEM_VERSION_LESS_THAN(@"8.0")){
            layerRect = CGRectMake(0, 0, self.view.bounds.size.height, self.view.bounds.size.width - kCameraToolBarHeight);
        }
        
        [[_captureManager previewLayer] setBounds:layerRect];
        [[_captureManager previewLayer] setPosition:CGPointMake(CGRectGetMidX(layerRect),CGRectGetMidY(layerRect))];
        [[[self view] layer] addSublayer:[[self captureManager] previewLayer]];
        
        UIImage *gridImage;
        
        if ([[UIScreen mainScreen] bounds].size.height == 568.000000)
        {
            gridImage = [UIImage imageNamed:@"cameraOverlay1.png"];
        }
        else
        {
            gridImage = [UIImage imageNamed:@"cameraOverlay1.png"];
        }
        
//        [self.view.layer addSublayer:[self drawRectangleForCameraOverlay]];
      //  [self drawRectangle:YES];
        
        
        
        UIImageView *gridCameraView = [[UIImageView alloc] initWithImage:gridImage];
        [gridCameraView setFrame:CGRectMake(10, 0, gridImage.size.width, gridImage.size.width)];
        
        UISwipeGestureRecognizer *swipeDown = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(dismissMAImagePickerController)];
        [swipeDown setDirection:UISwipeGestureRecognizerDirectionDown];
        [self.view addGestureRecognizer:swipeDown];
        
//        [[self view] addSubview:gridCameraView];
        
//        GridView *gridView=[[GridView alloc]initWithFrame:CGRectMake(0, 0, 200, 400)];
//        [gridView drawRect:CGRectMake(0, 0, 200, 400)];
//        [self.view addSubview:gridView];
        
        
        
        _cameraToolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.height - kCameraToolBarHeight, self.view.bounds.size.width, kCameraToolBarHeight)];
        if(SYSTEM_VERSION_LESS_THAN(@"8.0")){
            _cameraToolbar = [[UIToolbar alloc] initWithFrame:CGRectMake(0, self.view.bounds.size.width - kCameraToolBarHeight, self.view.bounds.size.height, kCameraToolBarHeight)];
        }
        
        
        [_cameraToolbar setBackgroundImage:[UIImage imageNamed:@"camera-bottom-bar"] forToolbarPosition:UIToolbarPositionAny barMetrics:UIBarMetricsDefault];
        
        UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"close-button"] style:UIBarButtonItemStylePlain target:self action:@selector(dismissMAImagePickerController)];
        cancelButton.accessibilityLabel = @"Close Camera Viewer";
        
        UIImage *cameraButtonImage = [UIImage imageNamed:@"camera-button"];
        UIImage *cameraButtonImagePressed = [UIImage imageNamed:@"camera-button-pressed"];
        UIButton *pictureButtonRaw = [UIButton buttonWithType:UIButtonTypeCustom];
        [pictureButtonRaw setImage:cameraButtonImage forState:UIControlStateNormal];
        [pictureButtonRaw setImage:cameraButtonImagePressed forState:UIControlStateHighlighted];
        [pictureButtonRaw addTarget:self action:@selector(pictureMAIMagePickerController) forControlEvents:UIControlEventTouchUpInside];
        pictureButtonRaw.frame = CGRectMake(0.0, 0.0, cameraButtonImage.size.width, cameraButtonImage.size.height);
        _pictureButton = [[UIBarButtonItem alloc] initWithCustomView:pictureButtonRaw];
        _pictureButton.accessibilityLabel = @"Take Picture";
        
        if ([[NSUserDefaults standardUserDefaults] objectForKey:kCameraFlashDefaultsKey] == nil)
        {
            [self storeFlashSettingWithBool:YES];
        }
        
        if ([[NSUserDefaults standardUserDefaults] boolForKey:kCameraFlashDefaultsKey])
        {
            _flashButton = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"flash-on-button"] style:UIBarButtonItemStylePlain target:self action:@selector(toggleFlash)];
            _flashButton.accessibilityLabel = @"Disable Camera Flash";
            flashIsOn = YES;
            [_captureManager setFlashOn:YES];
        }
        else
        {
            _flashButton = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"flash-off-button"] style:UIBarButtonItemStylePlain target:self action:@selector(toggleFlash)];
            _flashButton.accessibilityLabel = @"Enable Camera Flash";
            flashIsOn = NO;
            [_captureManager setFlashOn:NO];
        }
        
        UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        UIBarButtonItem *fixedSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
        [fixedSpace setWidth:10.0f];
        
        [_cameraToolbar setItems:[NSArray arrayWithObjects:fixedSpace,cancelButton,flexibleSpace,_pictureButton,flexibleSpace,_flashButton,fixedSpace, nil]];
        
        [self.view addSubview:_cameraToolbar];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(transitionToMAImagePickerControllerAdjustViewController) name:kImageCapturedSuccessfully object:nil];
        
        _cameraPictureTakenFlash = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height -kCameraToolBarHeight)];
        [_cameraPictureTakenFlash setBackgroundColor:[UIColor colorWithRed:0.99f green:0.99f blue:1.00f alpha:1.00f]];
        [_cameraPictureTakenFlash setUserInteractionEnabled:NO];
        [_cameraPictureTakenFlash setAlpha:0.0f];
        [self.view addSubview:_cameraPictureTakenFlash];
    }
    else
    {
        self.view.layer.cornerRadius = 8;
        self.view.layer.masksToBounds = YES;
        
        _invokeCamera = [[UIImagePickerController alloc] init];
        _invokeCamera.delegate = self;
        _invokeCamera.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        _invokeCamera.allowsEditing = NO;
        [self.view addSubview:_invokeCamera.view];
    }
    UIBezierPath *path = nil;
    int yPosition=(self.view.bounds.size.height -kCameraToolBarHeight)-200;
    if(SYSTEM_VERSION_LESS_THAN(@"8.0")){
       yPosition=(self.view.bounds.size.width -kCameraToolBarHeight)-200;
    path = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(40, yPosition/2-10, self.view.frame.size.height-80, 230) cornerRadius:0];

    }
    else
    {
        NSLog(@"View frame = %@",NSStringFromCGRect(self.view.frame));
        path = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(40, yPosition/2-20, self.view.frame.size.width-80, 230) cornerRadius:0];
    }
    //    UIBezierPath *circlePath = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(10, 0, 300,300) cornerRadius:5];
    //    [path appendPath:circlePath];
    [path setUsesEvenOddFillRule:YES];
    
    self.fillLayer = [CAShapeLayer layer];
    self.fillLayer.path = path.CGPath;
    self.fillLayer.fillRule = kCAFillRuleEvenOdd;
    self.fillLayer.fillColor = [UIColor yellowColor].CGColor;
    self.fillLayer.opacity = 0.2;
    [self.view.layer addSublayer:self.fillLayer];
}


-(void) orientationChanged
{
    //return;
//    UIDeviceOrientation deviceOrientation = [UIDevice currentDevice].orientation;
//    if (deviceOrientation == UIInterfaceOrientationPortraitUpsideDown || deviceOrientation == UIInterfaceOrientationPortrait) {
//        NSLog(@"orientationChanged changed to Portrait");
//        [self resetPath:NO];
//    }
//    else {
        NSLog(@"orientationChanged changed to Landscape");
        //[self resetPath:YES];
//    }
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
    if (fromInterfaceOrientation == UIInterfaceOrientationLandscapeLeft || fromInterfaceOrientation == UIInterfaceOrientationLandscapeRight) {
        // change positions etc of any UIViews for Landscape
        NSLog(@"didRotateFromInterfaceOrientation: Landscape");
        
    } else {
        // change position etc for Portait
        NSLog(@"didRotateFromInterfaceOrientation: Portait");
       
    }
}

-(void) resetPath:(BOOL) isLandscape {
    [UIView animateWithDuration:0.5 animations:^{
        if (isLandscape) {
            int yPosition=(self.view.bounds.size.width -kCameraToolBarHeight)-200;
            UIBezierPath *path =[UIBezierPath bezierPathWithRoundedRect:CGRectMake(30, yPosition/2+70, 250, 350) cornerRadius:0];
            self.fillLayer.path=path.CGPath;
            
        } else {
            int yPosition=(self.view.bounds.size.height -kCameraToolBarHeight)-200;
            UIBezierPath *path =[UIBezierPath bezierPathWithRoundedRect:CGRectMake(10, yPosition/2, 300, 200) cornerRadius:0];
            self.fillLayer.path=path.CGPath;
        }
        
        [self.view.layer setNeedsDisplay];
    } completion:^(BOOL finished){
    }];
    return;

}
-(void)drawRectangle:(BOOL) isLandscape
{
    NSLog(@"No of sublayers = %d",self.view.layer.sublayers.count);
    
//    if (isLandscape) {
        //    int radius = 100;
        int yPosition=(self.view.bounds.size.width -kCameraToolBarHeight)-200;
        UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(30, yPosition/2, 250, 350) cornerRadius:0];
        //    UIBezierPath *circlePath = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(10, 0, 300,300) cornerRadius:5];
        //    [path appendPath:circlePath];
        [path setUsesEvenOddFillRule:YES];
        
        CAShapeLayer *fillLayer = [CAShapeLayer layer];
        fillLayer.path = path.CGPath;
        fillLayer.fillRule = kCAFillRuleEvenOdd;
        fillLayer.fillColor = [UIColor yellowColor].CGColor;
        fillLayer.opacity = 0.2;
        [self.view.layer addSublayer:fillLayer];
//    } else {
//        //    int radius = 100;
//        int yPosition=(self.view.bounds.size.height -kCameraToolBarHeight)-200;
//        
//        UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(10, yPosition/2, 300, 200) cornerRadius:0];
//        //    UIBezierPath *circlePath = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(10, 0, 300,300) cornerRadius:5];
//        //    [path appendPath:circlePath];
//        [path setUsesEvenOddFillRule:YES];
//        
//        CAShapeLayer *fillLayer = [CAShapeLayer layer];
//        fillLayer.path = path.CGPath;
//        fillLayer.fillRule = kCAFillRuleEvenOdd;
//        fillLayer.fillColor = [UIColor yellowColor].CGColor;
//        fillLayer.opacity = 0.2;
//        [self.view.layer addSublayer:fillLayer];
//    }
}


-(CAShapeLayer*) drawRectangleForCameraOverlay {
    
    //250.0 => bottom line
    //310.0 => right line
    
    
    
    // Create a UIBezierPath (replace the coordinates with whatever you want):
    UIBezierPath *path = [UIBezierPath bezierPath];
    [path moveToPoint:CGPointMake(10.0, 10.0)];
    
    // # Entry Point with 10px white frame
    [path moveToPoint:CGPointMake(10.0, 10.0)];
    
    // # Keeping 10px frame with iPhone's 450 on y-axis
    [path addLineToPoint:CGPointMake(10.0, 250.0)];
    
    // # Substracting 10px for frame on x-axis, and moving 450 in y-axis
    [path addLineToPoint:CGPointMake(310.0, 250.0)];
    
    // # Moving up to 1st step 10px line, 310px on the x-axis
    [path addLineToPoint:CGPointMake(310.0, 10.0)];
    
    // # Back to entry point
    [path addLineToPoint:CGPointMake(10.0, 10.0)];
    
    // 4th Create a CAShapeLayer that uses that UIBezierPath:
    CAShapeLayer *shapeLayer = [CAShapeLayer layer];
    shapeLayer.path = [path CGPath];
    shapeLayer.strokeColor = [[UIColor blueColor] CGColor];
    shapeLayer.lineWidth = 2.0;
    shapeLayer.fillColor = [[UIColor clearColor] CGColor];
    
    // 5th add shapeLayer as sublayer inside layer view
    return shapeLayer;
//    [self.view.layer addSublayer:shapeLayer];
}

- (void)viewDidAppear:(BOOL)animated
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(resetFrameCount:) name:kResetFrameCapture object:nil];
    _isUploadingDone  = YES;
    // uploaded image to server didn't have required confidence level
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(startCaptureSession) name:@"MAImageNotHaveRequiredConfidanceLevel" object:nil];
    
    if(_activityIndicator){
    [_activityIndicator stopAnimating];
    _activityIndicator.hidden = YES;
    }
    
    if (_sourceType == MAImagePickerControllerSourceTypeCamera && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(pictureMAIMagePickerController)
                                                     name:@"AVSystemController_SystemVolumeDidChangeNotification"
                                                   object:nil];
        
        [_pictureButton setEnabled:YES];
        [[_captureManager captureSession] startRunning];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    if (_sourceType == MAImagePickerControllerSourceTypeCamera && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:@"AVSystemController_SystemVolumeDidChangeNotification" object:nil];
        
        //[[_captureManager captureSession] stopRunning];
    }
}

- (void)pictureMAIMagePickerController
{
    NSLog(@"pictureMAIMagePickerController");
    if (![[_captureManager captureSession] isRunning]) {
        return;
    }
    
    [_pictureButton setEnabled:NO];
    [_captureManager captureStillImage];
}

- (void)toggleFlash
{
    if (flashIsOn)
    {
        flashIsOn = NO;
        [_captureManager setFlashOn:NO];
        [_flashButton setImage:[UIImage imageNamed:@"flash-off-button"]];
        _flashButton.accessibilityLabel = @"Enable Camera Flash";
        [self storeFlashSettingWithBool:NO];
    }
    else
    {
        flashIsOn = YES;
        [_captureManager setFlashOn:YES];
        [_flashButton setImage:[UIImage imageNamed:@"flash-on-button"]];
        _flashButton.accessibilityLabel = @"Disable Camera Flash";
        [self storeFlashSettingWithBool:YES];
    }
    
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    if ([device hasTorch]) {
        [device lockForConfiguration:nil];
        [device setTorchMode:flashIsOn?AVCaptureTorchModeOn:AVCaptureTorchModeOff];  // use AVCaptureTorchModeOff to turn off
        [device unlockForConfiguration];
    }

}

- (void)storeFlashSettingWithBool:(BOOL)flashSetting
{
    [[NSUserDefaults standardUserDefaults] setBool:flashSetting forKey:kCameraFlashDefaultsKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

//start capture session.
-(void)startCaptureSession{
    _isUploadingDone = YES;
    _adjustViewController = nil;
    [_activityIndicator stopAnimating];
    _activityIndicator.hidden = YES;
    if(!_failedImageUploadIcon){
        _failedImageUploadIcon = [[UIImageView alloc] initWithFrame:_activityIndicator.frame];
        [self.view addSubview:_failedImageUploadIcon];
        _failedImageUploadIcon.image = [UIImage imageNamed:@"failed_icon.png"];
        [self.view bringSubviewToFront:_failedImageUploadIcon];
    }
    _failedImageUploadIcon.hidden = NO;
}

//showing some upload indicator
-(void)imageUploadingStart{
    if(!_activityIndicator)
    {
        _activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        CGRect frame = _activityIndicator.frame;
        frame.origin.x = self.view.frame.size.width - frame.size.width-5;
        frame.origin.y = 5;
        _activityIndicator.frame = frame;
        [self.view addSubview:_activityIndicator];
    }
    _activityIndicator.hidden = NO;
    [self.view bringSubviewToFront:_activityIndicator];
    [_activityIndicator startAnimating];
}

-(void)imageAccepted:(NSNotification *)notification {
    
    NSLog(@"%@", notification.userInfo);
    NSLog(@"%@", [notification.userInfo objectForKey:@"meanConfidence"]);

    MAInformationViewController *imageInfo = [[MAInformationViewController alloc] initWithNibName:@"MAInformationViewController" bundle:nil];
    imageInfo.infoDict = notification.userInfo;
    [self.navigationController pushViewController:imageInfo animated:NO];
    
}

//TODO: Edit this method and start this task in Background
- (void)transitionToMAImagePickerControllerAdjustViewController
{
    //TODO: this session can be stop once image is accepted by server with required acceptance level.
    //[[_captureManager captureSession] stopRunning];
    
    /* 
     
     Here instead of going to MAImagePickerControllerAdjustViewController controller we do all the
     work of this controller here itself, so that user always have camera picker view.
     
    */
    
    if(_isUploadingDone){
        NSLog(@"_isUploaingDone is True");
        _failedImageUploadIcon.hidden = YES;
        NSLog(@"View Frame before creating imageAdjustView = %@", NSStringFromCGRect(self.view.frame));
        if(!_imageAdjustView){
            _imageAdjustView = [[UIView alloc] initWithFrame:self.view.frame];
            CGRect frame = self.view.frame;
            frame.origin.y = - frame.size.height;
            _imageAdjustView.frame = frame;
            _imageAdjustView.backgroundColor = [UIColor grayColor];
            [self.view addSubview:_imageAdjustView];
        }
        if(!_adjustViewController)
        {
            _adjustViewController = [[MAImagePickerControllerAdjustViewController alloc] init];
        }
        _adjustViewController.sourceImage = [[self captureManager] stillImage];
        _adjustViewController.imageAdjustView = _imageAdjustView;
        NSLog(@"stillImage = %@",[[self captureManager] stillImage]);
        [UIView animateWithDuration:0.05 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut animations:^
         {
             _cameraPictureTakenFlash.alpha = 0.5f;
         }
                         completion:^(BOOL finished)
         {
             [UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut animations:^
              {
                  _cameraPictureTakenFlash.alpha = 0.0f;
              }
                              completion:^(BOOL finished)
              {
                  CATransition* transition = [CATransition animation];
                  transition.duration = 0.4;
                  transition.type = kCATransitionFade;
                  transition.subtype = kCATransitionFromBottom;
                  //halt capture session and upload image.
                  //[[_captureManager captureSession] stopRunning];
                  _isUploadingDone = NO;
                  [_adjustViewController setUpAdjustImageView];
              }];
         }];
    }
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    //[self dismissMAImagePickerController];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    [_invokeCamera removeFromParentViewController];
    imagePickerDismissed = YES;
    [self.navigationController popViewControllerAnimated:NO];
    
    MAImagePickerControllerAdjustViewController *adjustViewController = [[MAImagePickerControllerAdjustViewController alloc] init];
    adjustViewController.sourceImage = [[info objectForKey:UIImagePickerControllerOriginalImage] fixOrientation];
    
    CATransition* transition = [CATransition animation];
    transition.duration = 0.4;
    transition.type = kCATransitionFade;
    transition.subtype = kCATransitionFromBottom;
    [self.navigationController.view.layer addAnimation:transition forKey:kCATransition];
    [self.navigationController pushViewController:adjustViewController animated:NO];
    
}


- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo
{
    if (error != NULL) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error!" message:@"Image couldn't be saved" delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:nil, nil];
        [alert show];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)dealloc
{
    _captureManager = nil;
}

- (void)dismissMAImagePickerController
{
    [self removeNotificationObservers];
    if (_sourceType == MAImagePickerControllerSourceTypeCamera && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        //[[_captureManager captureSession] stopRunning];
        AudioSessionSetActive(NO);
    }
    else
    {
        [_invokeCamera removeFromParentViewController];
    }
    
    [_delegate imagePickerDidCancel];
}

- (void) MAImagePickerChosen:(NSNotification *)notification
{
    AudioSessionSetActive(NO);
    // Don't want to remove notification now!!!
    //[self removeNotificationObservers];
    [_delegate imagePickerDidChooseImageWithPath:[notification object]];
}


- (void)removeNotificationObservers
{
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskPortrait;
}

- (BOOL)shouldAutorotate
{
    return NO;
}

-(void) resetFrameCount:(NSNotification *) notification {
    MAAppDelegate *appDelegate=(MAAppDelegate*)[[UIApplication sharedApplication]delegate];
    if (notification) {
        NSDictionary* userInfo = [notification userInfo];
        NSNumber *result=(NSNumber*)[userInfo objectForKey:@"result"];
        
        if ([result boolValue]) {
            NSLog(@"SUCCES");
            [[NSNotificationCenter defaultCenter] postNotificationName:kImageCapturedSuccessfully object:nil];
        }else {
            NSLog(@"FAILED");
            appDelegate.isProcessing=NO;
        }
    }else
        appDelegate.isProcessing=NO;
}


@end
