Kashiwamochi.jar: Kashiwamochi.class images/*.gif
	..\bin\jar cfm Kashiwamochi.jar MANIFEST.MF *.class images/*.gif

Kashiwamochi.class: Kashiwamochi.java
	..\bin\javac Kashiwamochi.java
