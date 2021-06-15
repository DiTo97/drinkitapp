# DrinkItApp

**Authors:** F. Minutoli, L. Ratto, I. Clivio, and G. Picci.

Alcohol abuse results in physical harm and mental malfunction and is responsible for 1 in 10 deaths among adults aged 20-64 years in most Western countries annually, resulting in a grand total of almost 90.000 individuals in the United States alone. Moreover, binge drinking (defined as 4 or more drinks for women on a single occasion, and 5 or more drinks for men on a single occasion) has been steadily on the rise among youngsters. Between 2014 and 2017, over a third of college students aged 18-20 reported binge drinking in the prior month. Even though it’s well known that alcohol impairs driving ability, many people frequently drive when drunk. In 2010, 47.2% of pedestrian fatalities and 39.9% of vehicle occupant fatalities were caused by drunk driving. However, in many Driving Under the Influence (DUI) cases, drivers are not even aware that they are over the legal driving limit of their region.

Alcohol consumption raises the Blood Alcohol Content (BAC) of drinkers, impacting their neuromotor and cognitive functions approximately 20 minutes after alcohol consumption.
The BAC level measures the amount of alcohol in the blood, therefore traveling through the body to every organ, including the brain. In its simplest form, calculating a person's BAC level is based on how much alcohol went into what kind of body over a period of how much time.

Still much needs to be done to raise awareness on the risks that alcohol abuse still poses in the late 2010s and why self-monitoring is a crucial step in all kinds of DUI prevention.

## BSc thesis objective

This project revolves around the development of an Android mobile app, later named DrinkItApp, to encourage alcohol users' self-monitoring and to prevent DUI cases. It sports an easy-to-use and minimalistic interface to appear attractive to the users after repeated use, while facilitating the UX in case of intoxication. 

DrinkItApp settles in the niche/utility market sector, and as such, attractiveness is the key factor to boost its usage. That's why it has been designed as a daily diary to fill in with what has been eaten and what has been drunk, through which the BAC can be estimated in real-time, while providing useful statistics about a person's drinking habits. The benefits of the daily diary are two-fold, as there is another big advantage that it brings under the hood. Indeed the app implements a revamp of E. Widmark's original deterministic model for BAC estimation from the 1930s, as described in D. Posey and A. Mozayani, The Estimation of Blood Alcohol Concentration, 2007. All these deterministic models share a common feature... _Intrusiveness_. Indeed, not only they require knowledge of a person's body and age characteristics, but also of a detailed sequence of timestamped drinks over the course of a day in order to produce a somewhat reliable estimate. In this context the daily diary is a nice excuse that enables all of that for free.

This Android app features a proprietary NodeJS backend interacting with Firebase cloud services, for monitoring the BAC level in peoples’ body. It is enriched with functionalities aimed at preventing damage to them, such as a Reflex mini-game, and the possibility to call emergency and mobility services nearby through GPS (i.e., Uber, police, ambulance). Another key feature of the app is modularity, as it implements a pseudo-MVC pattern. This choice proved to be advantageous and, in particular, it allowed us to build the BAC estimation module as a standalone module from the core app logic. This also means that the BAC estimation model can be easily substituted with something else, if that necessity will ever ask for it, for instance introducing the concepts of the first AI models that are seeing birth in the field granted knowledge of the drinking habits of a person, his/her gait patterns and transdermal biometrics.

**Please note:** This codebase has not been maintained for a long time, as later iterations of the project have been moved to a different location following its release on the Play Store.

## Acknowledgements

We would like to acknowledge the University of Genoa for letting us work on this project, and Z. Collins, owner of [TheCocktailDb](https://www.thecocktaildb.com/), for letting us use all its drinks, ingredients and pictures for free from its immense database.
