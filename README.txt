All tasks have been implemented for this assignment. My page-i will render the Dinosaur (trex.obj sourced from TurboSquid.com) that I used in Assignment 3, but this time he is lit up with three lights. The lights are off-screen, but do light him up.

Flat, Gouraud, and Phong lighting all working correctly. The only issue comes from Assignment 1, where you can see a few holes in the filled polygons.

Simp and OBJ files are in the src directory, and the directory above because running Main within Eclise seems to use a different working directory than when run from command line.

To run my project via command line, open a command line INSIDE src directory.

To compile (while inside src):		javac client/Main.java
To run:		java client.Main page-i

You can also try:

javac -cp ./src ./src/client/Main.java
java -cp ./src client.Main page-i

You can remove page-i and it will default to first page.
You can change Page-a1A to any pages from A through J, and it will render that page. From this page, you can still hit Next Page and it will go to next appropriate page.

Extra feature:
If you type in gibberish instead of a page, it will default to first page.