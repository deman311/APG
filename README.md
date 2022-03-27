# APG - Automaic prnt.sc Generator
An application for generating random print-screen images from the LightShot site.

## Introduction ⭐
So there is this site / app called LightShot at https://prnt.sc , this platform allows you to take screenshots easily and quickly, One side-effect though is that everytime you do, the site generates the screenshot at a random link and sends is to you for ease-of-use.

Some people on the internet have figured out that the link is a combination of 6 characters and/or numbers - knowing this, you can randomly type a combination out of the top of your head and it will give you someone's random screenshot.

So I figured out that I can write a function that will generate you a given amount of pictures at a click of a botton so that you can explore the random print-screens more throughtly. This first started as a side function of my other project 'CoronaBot' (check out my other repository: https://github.com/deman311/CoronaBot), as a Discord bot feature and it would generate you 5 random pictures each time you called the command '!prnt'.

#### Old Discord Implementation
![discord bot prnt gif](/git_assets/olddiscordprnt.gif "The old days as part of CoronaBot")

## JavaFX Application 🎨
After seeing that me and a few friends liked the idea because it had some kind of a 'chance game' element to it, I decided to upgrade the project and create a dedicated application for the feature using JavaFX. At first the application had the same feature as the bot with a single button called
'GIVE ME 5' and it was just a UI upgrade. Quickly enough I've added an update that included a slider that you can choose an amount from 6 - 100 picture to generate for each click. The application would simply use Jsoup in order to access the site 'behaving' like a client browser and save the picture locally in a dedicated folder.

![application showcase gif](/git_assets/nothreading.gif "The unthreaded version - very slow!")

### Threading 👥
After a couple of updates for making the UI look better and other things like adding another window that includes a soundboard with dumb sounds for a friend, I came up with the idea that if I use threading and not just access each image in order, I could speed up the process. The initial test included 20 threads for a 100 images and the results looked very promising but resulted in an IP auto-ban by a bot from the site, this is because I was accessing the site with all of these threads simultaniously from the same IP and the security bot probably flagged me as a DDOS attack of sort. Thankfully the ban was for 24 hours by which I got a VPN program for testing in case they try to IP ban me again. I started testing out different thread counts to see which one results in the best time and also doesn't auto-bans me and finally settled on a sweet-spot of 5 threads. Now my application was 'pulling' images from the site more than 5 times faster than before which allowed for a much smoother user experience.

            int REMAIN = TOTAL % MAX_THREADS;
            for (int i = 0; i < MAX_THREADS; i++) {
                if (TOTAL / MAX_THREADS == 0) {
                    RANGE = TOTAL % MAX_THREADS;
                    i = MAX_THREADS - 1; // make it finish using only 1 thread
                }
                if (TOTAL % MAX_THREADS == 0) {
                    RANGE = TOTAL / MAX_THREADS;
                } else {
                    if (i < MAX_THREADS - 1 && REMAIN > 0) {
                        RANGE = (int) Math.floor(TOTAL / MAX_THREADS) + 1; // add it to the REMAIN
                        REMAIN--;
                    } else
                        RANGE = (int) Math.floor(TOTAL / MAX_THREADS);
                }

                int finalRANGE = RANGE, finalI = i;
                Thread t = new Thread(() -> {
                    for (int k = 0; k < finalRANGE; k++) {

![faster application showcase](/git_assets/threading.gif "The threaded version - much faster!")

### Banned Images ⛔
A lot of images on the site look like this: [banned image 1], because of that, I decided that I want to filter them out when generating images so that the experience won't be tedious or repetitive. What I did was to write a code that scans each images pulled in contrast to a bank of images that are considered banned and compares their pixels. If there is a match, the image is not printed and instead another image is tried.

![how many banned images currently - picture](/git_assets/bannedimgs.png "So many banned templates...")

This technique was working but not as well as I expected, there were variations of the banned images i.e., different crops, slighltly different shading and brightness or contrast, this resulted in some images still passing my filter. This made me come up with the idea of instead of looking for a perfect match, to count the similar pixels in the image to the banned one and have a sort of 'similarity precentage', this way I could have a variable to control the 'ban threshold' which resulted in better filtration and less banned images variations coming through.

#### Crypto Frauds 🪙
One of the things we noticed when going through the images on the site was that there were quite a few similar images for the porpuse of fraud. The people who uploaded these images made them look like a mistake of a screenshot containing tens of thousands of dollars worth of cryptocurrency containing account details and when people tried inserting credit card details in order to pass the money to themselves - their details were being stolen.

I'm explaining this because there are A LOT of these images on the site in order to fool people, these turned out to be the majority of the banned images and these were also very similar looking but also more variable. e.g:
![geoimg1](/git_assets/geo1.png "Damn fraudsters")![geoimg2](/git_assets/geo2.png "Damn fraudsters")

Because of that, I've decided to test out an idea that was a technique I learned in Convolutional Networks that is a Pooling layer that downscales images to try and encode the relevant information using less data. What I did was to take all of the banned images bank and downscale it by a factor of 3, then when comparing to the image pulled from the site, I would also temporarily downscale it by the same factor. Using this technique with the 'similarity precentage' and also threashold resulted in an amazing level of filtering, the current version filters out more than 99.7% of banned images.

The reason this technique works so well as I anticipated is because when downscaling the images, the text becomes unreadable / irrelevant so when comparing pixels you can almost all of the times make out the format that you want to ban when comparing to the bank and not specifically a given text / image.

![banned template](/git_assets/bantemplate.png "This is a real template")

### Encoding ⛓️
After finding various interesting images I wanted to have some way to backtrack their link because each time me and my friends will send them to each other we will only send the image and not the link. I came up with the idea to write a code that encodes the link inside the pictures by allocating some pixels on the edges to certain values that can later be decoded as the original link code of the image on the site, the pixels themsleves are also interpreted by the image format as colors and are almost always unnoticeable.

![decoding gif](/git_assets/decode.gif "Very neat and fast")

## Virtual Reality 🕶️🎭
I decided I want to get some experience with VR coding in Unity. This made me think of an interesting idea that I wanted to see if I can implement, I wanted to take the existing JavaFX application and include a VR feature for it. The idea was to take the pictures generated from the application, send them in real-time into a simultaniously running Unity application which in turn will build a 3-demensional museum corridor of sort, that will be filled with pictures on the walls, these pictures being the APG images.

[VR application Gif] -> TBA

This little 'expansion' was a great experience because not only did I get to write my first VR application and study some VR development - being a VR gamer myself, but also this helped me sharpen my skills with Java Networking features, I used a listener Server and Client approach in order to sent commands to the Unity application that behaves as a server from the APG application that behaved as a client.

            while (true)
            {
                        Debug.Log("Awaiting connections...");
                        TcpClient client = null;
                        await Task.Run(() =>
                        {
                            client = server.AcceptTcpClient();
                        });
                        Debug.Log("Connected!");

                        NetworkStream stream = client.GetStream();
                        byte[] buff = new byte[1024];
                        int bytesRead = stream.Read(buff, 0, buff.Length);
                        string img_path = Encoding.ASCII.GetString(buff, 0, bytesRead);
                        Debug.Log("Message recieved: " + img_path);

                        if (img_path.Contains("@RESTART@") || img_path.Contains("@READY@"))
                        {

                            if(img_path.Contains("@READY@")) {
                                /*devices.Clear();
                                InputDevices.GetDevices(devices);
                                InputDevices.GetDevicesWithCharacteristics(InputDeviceCharacteristics.Right, devices);
                                InputDevice rightCon = devices[0];*/

                                mainText.SetText("GIVE ME MORE");
                                gib_btn.gameObject.SetActive(true);
                                bool isPressed = false;
                                gib_btn.onClick.AddListener(() =>
                                {
                                    EventSystem.current.SetSelectedGameObject(null); // remove button highlight
                                    mainText.SetText("Loading...");
                                    isPressed = true;
                                    gib_btn.gameObject.SetActive(false);
                                });

### [DISCLAIMER] This application may contain disturbing and inappropriate images such as gore, porn and everything else there is on the internet.
I have no control as for what images appear as the program randomly generates all the links.
