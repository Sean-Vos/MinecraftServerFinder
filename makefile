WindowsBuild = false

build: ServerFinder.class SearchThread.class ListServer.class
	jar cfm MinecraftServerFinder.jar mf.txt json_simple-1.1.jar ServerFinder.class SearchThread.class \
	ListServer.class

ServerFinder.class: ServerFinder.java ListServer.java SearchThread.java
	javac -cp json_simple-1.1.jar ServerFinder.java SearchThread.java ListServer.java
	
clean:
ifeq ($(WindowsBuild), true)
	del ListServer.class
	del SearchThread.class
	del ServerFinder.class
	del MinecraftServerFinder.jar
else
	rm ./ListServer.class
	rm ./SearchThread.class
	rm ./ServerFinder.class
	rm ./MinecraftServerFinder.jar
endif