#Building
If you are on windows change the `WindowsBuild` variable in the make file to equal `true`. Otherwise just run make to build the jar file.
Run 
> make clean
to clean up the repo directory of build files

#Using The Program
Enter the directory of `MinecraftServerFinder.jar` in the command line.
Make a text file that contains Ip ranges separated by new line characters.
A ip rangle looks like this: `0.0.0.0-255.255.255.255` (don't use this, this is just an example)
You can get ip ranges of a specific area at [Super Ip](https://suip.biz)
Next make sure that `List.html` is in the same directory as the `MinecraftServerFinder.jar` file.
You can now run the jar file by running the following command in the command line: `java -jar MinecraftServerFinder.jar [Ip ranges file path] [output file path] [thread count as an non negative integer value]`