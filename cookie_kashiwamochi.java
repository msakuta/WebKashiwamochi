/** 
 * Kashiwamochi Game Ver0.62
 * See http://maglog.jp/gltest/ for details.
 * 
 * This code contains some part of example codes distributed with java 1.4
 * SDK. Copyright notice follows.
 */

/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

import netscape.javascript.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.applet.*;
import java.util.StringTokenizer;

public
class Kashiwamochi extends Applet implements Runnable, MouseListener, MouseMotionListener, ActionListener {
	static final int eraseEffectBit = 0x100;
	static final int fallEffectBit = 0x200;
	static final int fallMaxTime = 3;
	static final int rollMaxTime = 3;
	static final int maxScoreTexts = 4;
	static final int gaugeHeight = 128;
	Rectangle giveupButton = null;
	Rectangle pauseButton = null;

	int board[] = new int[7 * 7];

	int cellsize = 32;

	int cursorx, cursory = -1;
//	int erasex, erasey = -1;
	int eraseEffectTime = 0;
	int mouseTime = 0;
	int fallTime = 0;
	int rollx, rolly = -1;
	int rollTime = 0;
	int rollDirection = 0;
	float maingauge = 0;
	float subgauge = 0;
	boolean gameover = true;
	boolean practiceMode = false;
	int highscores[] = new int[10];
	int newscore = -1;

	Thread engine = null;       // Thread animating the images
	boolean userPause = false;  // True if thread currently paused by user
	int points = 0;             // score points
	int chain = 0;              // chain count
	int movement = 0;           // internal variable to count chains
	int level = 0;              // game level
	int erases = 0;             // internal variable to control level
	Image backbuffer;           // image buffer for double buffering
	Graphics backg;             // background offscreen
	Font normalFont = new Font("SansSerif", Font.PLAIN, 16);
	Font bigFont = new Font("SansSerif", Font.BOLD, 20);

	int scoreTexts[] = new int[maxScoreTexts * 4]; // r, c, point, life

    /**
     * The images
     */
    Image blockImage;
    Image aImage;
    Image bImage;
    Image cImage;
    Image dImage;
    Image eImage;
    Image lightImage;

	TextField tf1, tf2;
	Button b1, b2, b3;

    public int newblock(){
    	return (int)(Math.random() * 150) == 0 ? 1 : (int)(Math.random() * 5) + 2;
    }

    Image loadImage(String fname){
//    	return getImage(getCodeBase(), fname);
      	try{
		  	return getToolkit().getImage(new URL(getCodeBase(), fname));
		}catch(MalformedURLException err){}
		return null;
/*    	try{
//			showStatus(fname + " loading");
//			showStatus( getCodeBase().getPath().toString());
//			showStatus(Integer.toString(getCodeBase().getPort()));
			URL url = new URL("jar:", getCodeBase().getPath(), getCodeBase().getPort(), fname);
			showStatus(url.toString());
			getImage(url);
		}catch(MalformedURLException err){
		}catch(java.io.IOException err){}
		return null;*/
    }

    /**
     * Initialize the applet. Resize and load images.
     */
    public void init() {
	blockImage = loadImage("images/block.png");
	aImage = loadImage("images/a.png");
	bImage = loadImage("images/b.png");
	cImage = loadImage("images/c.png");
	dImage = loadImage("images/d.png");
	eImage = loadImage("images/e.png");
	lightImage = loadImage("images/light.png");

	// Though I want it to be corrected in one archive for distribution,
	// none of all the methods here worked to load resources in jar file.
/*	blockImage = getImage(getCodeBase(), "images/block.png");
	aImage = getImage(getCodeBase(), "images/a.png");
	bImage = getImage(getCodeBase(), "images/b.png");
	cImage = getImage(getCodeBase(), "images/c.png");
	dImage = getImage(getCodeBase(), "images/d.png");
	eImage = getImage(getCodeBase(), "images/e.png");
	lightImage = getImage(getCodeBase(), "images/light.png");*/
/*	try{
		blockImage = createImage((ImageProducer) new URL(getCodeBase(), "images/block.png").getContent());
		aImage = createImage((ImageProducer) new URL(getCodeBase(), "images/a.png").getContent());
		bImage = createImage((ImageProducer) new URL(getCodeBase(), "images/b.png").getContent());
		cImage = createImage((ImageProducer) new URL(getCodeBase(), "images/c.png").getContent());
		dImage = createImage((ImageProducer) new URL(getCodeBase(), "images/d.png").getContent());
		eImage = createImage((ImageProducer) new URL(getCodeBase(), "images/e.png").getContent());
		lightImage = createImage((ImageProducer) new URL(getCodeBase(), "images/light.png").getContent());
	}catch(MalformedURLException err){
	}catch(java.io.IOException err){}*/
/*	try{
		blockImage = getToolkit().getImage(new URL(getCodeBase(), "images/block.png"));
		aImage = getToolkit().getImage(new URL(getCodeBase(), "images/a.png"));
		bImage = getToolkit().getImage(new URL(getCodeBase(), "images/b.png"));
		cImage = getToolkit().getImage(new URL(getCodeBase(), "images/c.png"));
		dImage = getToolkit().getImage(new URL(getCodeBase(), "images/d.png"));
		eImage = getToolkit().getImage(new URL(getCodeBase(), "images/e.png"));
		lightImage = getToolkit().getImage(new URL(getCodeBase(), "images/light.png"));
	}catch(MalformedURLException err){
	}catch(java.io.IOException err){}*/

	giveupButton = new Rectangle(getSize().width - 64 - 2, getSize().height - 16 - 2, 64, 16);
	pauseButton = new Rectangle(getSize().width - 160 - 2, getSize().height - 16 - 2, 64, 16);

		backbuffer = createImage( getSize().width, getSize().height );
		backg = backbuffer.getGraphics();

	addMouseListener(this);
	addMouseMotionListener(this);

	Thread me = Thread.currentThread();
	me.setPriority(Thread.MIN_PRIORITY);
	userPause = false;

	{
/*		String cookie = getCookie(), me;
		StringTokenizer tokker = new StringTokenizer(cookie, ";");
		while(tokker.hasMoreTokens()) if((me = tokker.nextToken()) == "highscore"){
			break;
		}*/
		String cookie = getCookie("highscore");
		System.out.println("read: " + cookie);
		StringTokenizer tokker = new StringTokenizer(cookie, ",");
		for(int i = 0; tokker.hasMoreTokens() && i < highscores.length; i++){
			highscores[i] = Integer.parseInt(tokker.nextToken());
		}
	}
/*
    tf1 = new TextField(20);
    tf2 = new TextField(20);
    
    b1 = new Button("Write Cookie");
    b2 = new Button("Read Cookie");
    b3 = new Button("Delete Coookie");
    
    setLayout(new FlowLayout());
    add(tf1);
    add(tf2);
    add(b1);
    add(b2);
    add(b3);
    
    b1.addActionListener(this);
    b2.addActionListener(this);
    b3.addActionListener(this);*/
    }
    
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == b1) {
       /*  
       **  write a cookie
       **    computes the expiration date, good for 1 month
       */
       java.util.Calendar c = java.util.Calendar.getInstance();
       c.add(java.util.Calendar.MONTH, 1);
       String expires = "; expires=" + c.getTime().toString();

       String s1 = tf1.getText() + expires; 
       System.out.println(s1);
        
       JSObject myBrowser = JSObject.getWindow(this);
       JSObject myDocument =  (JSObject) myBrowser.getMember("document");
    
       myDocument.setMember("cookie", s1);
       }

    if (ae.getSource() == b2) {
       /*
       **   read a cookie
       */
       tf2.setText(getCookie());
       }

    if (ae.getSource() == b3) {
       /*
       **  delete a cookie, set the expiration in the past
       */
       java.util.Calendar c = java.util.Calendar.getInstance();
       c.add(java.util.Calendar.MONTH, -1);
       String expires = "; expires=" + c.getTime().toString();

       String s1 = tf1.getText() + expires; 
       JSObject myBrowser = JSObject.getWindow(this);
       JSObject myDocument =  (JSObject) myBrowser.getMember("document");
       myDocument.setMember("cookie", s1);
       }
    }

    public String getCookie() {
      /*
      ** get all cookies for a document
      */
      try {
        JSObject myBrowser = (JSObject) JSObject.getWindow(this);
        JSObject myDocument =  (JSObject) myBrowser.getMember("document");
        String myCookie = (String)myDocument.getMember("cookie");
        if (myCookie.length() > 0) 
           return myCookie;
        }
      catch (Exception e){
        e.printStackTrace();
        }
      return "?";
      }

     public String getCookie(String name) {
       /*
       ** get a specific cookie by its name, parse the cookie.
       **    not used in this Applet but can be useful
       */
       String myCookie = getCookie();
       String search = name + "=";
       if (myCookie.length() > 0) {
          int offset = myCookie.indexOf(search);
          if (offset != -1) {
             offset += search.length();
             int end = myCookie.indexOf(";", offset);
             if (end == -1) end = myCookie.length();
             return myCookie.substring(offset,end);
             }
          else 
            System.out.println("Did not find cookie: "+name);
          }
        return "";
        }

    public void destroy() {
        removeMouseListener(this);
    }

	void drawMainScreen(Graphics g){
		g.setColor(Color.black);
		g.setFont(bigFont);
		g.drawString("Game Over", 32, 20);
		g.setFont(normalFont);
		g.drawString("Click to Start", 32, 48);
		g.drawString("Right Click to Practice", 32, 64);
		for(int i = 0; i < highscores.length; i++){
			g.setColor(newscore == i ? Color.red : Color.black);
			g.drawString((i + 1) + ": " + Integer.toString(highscores[i]), 32, 96 + 15 * i);
		}
	}

	void drawPauseScreen(Graphics g){
		g.setColor(Color.black);
		g.setFont(bigFont);
		g.drawString("Paused", getSize().width / 2 - 32, getSize().height / 2);
	}

	void drawGameScreen(Graphics g){
	g.setColor(Color.black);
	for(int k = 0; k <= 7; k++){
		int xoff = cellsize * k;
		int yoff = cellsize * k;
		g.drawLine(xoff, 0, xoff, 7 * cellsize);
		g.drawLine(0, yoff, 7 * cellsize, yoff);
	}

	g.setFont(normalFont);
	g.drawString("score: " + Integer.toString(points), cellsize * 7 + 16, 16);
	g.drawString("chain: " + Integer.toString(chain), cellsize * 7 + 16, 16 + 16);
	g.drawString("level: " + Integer.toString(level + 1), cellsize * 7 + 16, 16 + 32);
	if(practiceMode)
		g.drawString("Practice Mode", cellsize * 7 + 16, 224);
	g.drawString("level: " + Integer.toString(level + 1), cellsize * 7 + 16, 16 + 32);

	g.setColor(Color.black);
	g.drawRect(cellsize * 7 + 16, 64, 16, gaugeHeight);
	g.setColor(Color.orange);
	g.fillRect(cellsize * 7 + 16, 64 + (int)(gaugeHeight * (1. - maingauge)), 16, (int)(gaugeHeight * maingauge));

	g.setColor(Color.black);
	g.drawRect(cellsize * 7 + 64, 64, 8, gaugeHeight);
	g.setColor(Color.cyan);
	g.fillRect(cellsize * 7 + 64, 64 + (int)(gaugeHeight * (1. - subgauge)), 8, (int)(gaugeHeight * subgauge));

	g.setColor(Color.black);
	g.drawRect(giveupButton.x, giveupButton.y, giveupButton.width, giveupButton.height);
	g.drawString("GIVE UP", giveupButton.x, giveupButton.y + giveupButton.height);
	g.drawRect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
	g.drawString("Pause", pauseButton.x, pauseButton.y + pauseButton.height);

	int i = 0;
	for (int r = 0 ; r < 7 ; r++) {
	    for (int c = 0 ; c < 7 ; c++, i++) {
	    	Image image;
			switch(board[r * 7 + c] & ~(eraseEffectBit + fallEffectBit)){
			case 1: image = blockImage; break;
			case 2: image = aImage; break;
			case 3: image = bImage; break;
			case 4: image = cImage; break;
			case 5: image = dImage; break;
			case 6: image = eImage; break;
			default: image = null;
			}
			if(image != null){
				final int dir[] = {
					-1,0,  0,-1,  0,1,  1,0,
					0,-1,  1,0,  -1,0,  0,1,
				};
				if(rollTime != 0 && rollx <= c && c <= rollx + 1 && rolly <= r && r <= rolly + 1)
				    g.drawImage(image, c*cellsize + 1 + (dir[(rollDirection * 4 + (r - rolly) * 2 + c - rollx) * 2] * ( - rollTime) * cellsize / rollMaxTime), r*cellsize + 1 + (dir[(rollDirection * 4 + (r - rolly) * 2 + c - rollx) * 2 + 1] * ( - rollTime) * cellsize / rollMaxTime), this);
				else
				    g.drawImage(image, c*cellsize + 1, r*cellsize + 1 + ((board[r * 7 + c] & fallEffectBit) != 0 ? -fallTime * cellsize / fallMaxTime : 0), this);
			}
			if((board[r * 7 + c] & eraseEffectBit) != 0){
			    g.drawImage(lightImage, c*cellsize + 1, r*cellsize + 1, this);
			}
	    }
	}

	if(0 <= cursorx && 0 <= cursory){
		g.setColor(Color.red);
		for(i = 0; i < 3; i++)
			g.drawRect(cursorx * cellsize + i, cursory * cellsize + i, 2 * cellsize - i * 2, 2 * cellsize - i * 2);
	}

	g.setFont(bigFont);
	for(i = 0; i < maxScoreTexts; i++) if(scoreTexts[i * 4 + 3] != 0){
		int x, y;
		x = scoreTexts[i * 4 + 1];
		x = x < 0 ? 0 : x;
		y = scoreTexts[i * 4 + 0] + scoreTexts[i * 4 + 3];
		y = y < 20 ? 20 : y;
		g.setColor(Color.black);
		g.drawString(Integer.toString(scoreTexts[i * 4 + 2]), x + 2, y + 2);
		g.setColor(Color.red);
		g.drawString(Integer.toString(scoreTexts[i * 4 + 2]), x, y);
	}
	}

    /**
     * Paint it.
     */
    public void update(Graphics realg) {
    Graphics g = backg;
	g.setColor(Color.white);
	g.fillRect(0, 0, getSize().width, getSize().height);
	if(gameover)
		drawMainScreen(g);
	else if(userPause)
		drawPauseScreen(g);
	else
		drawGameScreen(g);
	g.setColor(Color.black);
	g.setFont(normalFont);
	g.drawString("Ver0.62", 32, getSize().height);
	paint(realg);
    }

	public void paint( Graphics g ) {
		g.drawImage( backbuffer, 0, 0, this );
	}

    /**
     * The user has clicked in the applet. Figure out where
     * and see if a legal move is possible. 
     */
    public void mouseClicked(MouseEvent e) {
    	if(gameover){
    		gameover = false;
    		points = 0;
    		chain = 0;
    		level = 0;
    		erases = 0;
    		maingauge = .3f;
    		subgauge = 1.f;
			for(int r = 0; r < 7; r++) for(int c = 0; c < 7; c++){
				board[r * 7 + c] = 0;
			}
			practiceMode = e.getButton() != e.BUTTON1;
    	}
    	if(userPause || pauseButton.contains(e.getX(), e.getY())){
			synchronized(this) {
				userPause = !userPause;
				repaint();
				if(!userPause)
	                notifyAll();
			}
    	}
    	if(giveupButton.contains(e.getX(), e.getY()))
    		becomeGameover();
    	if(mouseTime != 0)
    		return;
		int x = e.getX() - cellsize / 2;
		int y = e.getY() - cellsize / 2;
		int r = y / cellsize;
		int c = x / cellsize;

		if(0 <= r && r < 7 && 0 <= c && c < 6){
			if(e.getButton() == e.BUTTON1){
				int a,b,f = 0, d = 0;
				a = board[r * 7 + c];
				b = board[r * 7 + c + 1];
				if(r != 6){
					f = board[(r + 1) * 7 + c + 1];
					d = board[(r + 1) * 7 + c];
				}
/*				else for(int n = r; 0 <= n; n--){
					board[n * 7 + c + 1] = (0 < n ? board[(n - 1) * 7 + c + 1] : newblock());
				}*/
				board[r * 7 + c] = b;
				if(r != 6){
					board[r * 7 + c + 1] = f;
					board[(r + 1) * 7 + c + 1] = d;
					board[(r + 1) * 7 + c] = a;
				}
				else{
					board[r * 7 + c + 1] = 0;
				}
				rollDirection = 0;
			}
			else{
				int a,b,f = 0, d = 0;
				a = board[r * 7 + c];
				b = board[r * 7 + c + 1];
				if(r != 6){
					f = board[(r + 1) * 7 + c + 1];
					d = board[(r + 1) * 7 + c];
				}
/*				else for(int n = r; 0 <= n; n--){
					board[n * 7 + c] = (0 < n ? board[(n - 1) * 7 + c] : newblock());
				}*/
				board[r * 7 + c + 1] = a;
				if(r != 6){
					board[r * 7 + c] = d;
					board[(r + 1) * 7 + c + 1] = b;
					board[(r + 1) * 7 + c] = f;
				}
				else{
					board[r * 7 + c] = 0;
				}
				rollDirection = 1;
			}
			rollx = c;
			rolly = r;
			rollTime = rollMaxTime;
			if(movement != 0)
				chain = 0;
			movement = 1;
		}
		mouseTime = rollMaxTime;
//		check();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /**
     * MouseMotionListener implementation
     */
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){
    	cursorx = (e.getX() - cellsize / 2) / cellsize;
    	cursory = (e.getY() - cellsize / 2) / cellsize;
    	if(6 <= cursorx || 7 <= cursory)
    		cursory = -1;
    }

	void becomeGameover(){
		gameover = true;
		newscore = -1;
		if(!practiceMode){
			for(int i = 0; i < highscores.length; i++) if(highscores[i] < points){
				for(int j = highscores.length - 1; i < j; j--)
					highscores[j] = highscores[j-1];
				highscores[i] = points;
				newscore = i;
				break;
			}

			/*  
			**  write a cookie
			**    computes the expiration date, good for 1 month
			*/
//			java.util.Locale.setDefault();
			java.util.Calendar c = java.util.Calendar.getInstance();
			c.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
			c.add(java.util.Calendar.YEAR, 1);
//			String expires = "; expires=" + c.getTime().toString();
			String expires = "; expires=" + new java.text.SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss 'GMT'", java.util.Locale.ENGLISH).format(c.getTime());

			String s1 = "highscore=";
			for(int i = 0; i < highscores.length; i++)
				s1 += Integer.toString(highscores[i]) + ",";
			s1 += expires; 
			System.out.println(s1);

			JSObject myBrowser = JSObject.getWindow(this);
			JSObject myDocument =  (JSObject) myBrowser.getMember("document");

			myDocument.setMember("cookie", s1);
		}
	}

    /**
     * Run the animation. This method is called by class Thread.
     * @see java.lang.Thread
     */
    public void run() {
        Thread me = Thread.currentThread();
	try {
            while (engine == me) {
				repaint();
				if(!gameover){
				fallCheck();		// fall flag check precedes
				check();

				float speed = fallTime != 0 || eraseEffectTime != 0 ? 0f : (level + 1) * (level % 6 == 5 ? .2f : 1f) * .005f;
				if(subgauge < speed){
					speed -= subgauge;
					subgauge = 0;
					speed *= .25;
					if(maingauge < speed){
						if(practiceMode)
							maingauge = 0f;
						else
							becomeGameover();
					}
					else
						maingauge -= speed;
				}
				else
					subgauge -= speed;

				if(eraseEffectTime != 0 && --eraseEffectTime == 0) for(int i = 0; i < 7 * 7; i++) if((board[i] & eraseEffectBit) != 0){
					board[i] = 0 /*&= ~eraseEffectBit*/;
				}
				if(fallTime != 0 && --fallTime == 0) for(int r = 6; 0 <= r; r--) for(int c = 0; c < 7; c++) if((board[r * 7 + c] & fallEffectBit) != 0){
					board[r * 7 + c] &= ~fallEffectBit;
				}
				if(rollTime != 0 && --rollTime == 0)
					check();
				if(mouseTime != 0)
					mouseTime--;
				if((level + 1) * 25 <= erases)
					level++;
				for(int i = 0; i < maxScoreTexts; i++) if(scoreTexts[i * 4 + 3] != 0){
					scoreTexts[i * 4 + 3]--;
				}
				}

				// Pause for duration or longer if user paused
				try {        
				    Thread.sleep(50);
				    synchronized(this) {
				        while (userPause) {
				            wait();
				        }
				    }
				}
				catch (InterruptedException e) {
				}
			}
		} finally {
		}
    }

    /**
     * Start the applet by forking an animation thread.
     */
    public void start() {
	engine = new Thread(this);
	engine.start();
	showStatus(getAppletInfo());
    }

    /**
     * Stop the insanity, um, applet.
     */
    public synchronized void stop() {
	engine = null;
         if (userPause) {
            userPause = false;
            notify();
        }
    }

	int eraseFrame = 0;

	void scorePoint(int cc, int r, int c){
		int point = (10 + (cc < 4 ? 0 : cc - 4) * 10) * (chain + 1) * (level + 1);
		points += point;
		movement = 0;
		eraseEffectTime = 10;
		if(1 <= chain)
			maingauge = maingauge + .05f < 1f ? maingauge + .05f : 1f;
		if(eraseFrame == 0){
			eraseFrame = 1;
			chain++;
		}
		for(int i = 0; i < maxScoreTexts; i++) if(scoreTexts[i * 4 + 3] == 0){
			scoreTexts[i * 4 + 0] = r + 20;
			scoreTexts[i * 4 + 1] = c - 15;
			scoreTexts[i * 4 + 2] = point;
			scoreTexts[i * 4 + 3] = 20;
			break;
		}
		erases += cc;
		subgauge = 1f;
	}

	void fallCheck(){
		int r, c;

		if(eraseEffectTime == 0 && fallTime == 0 && rollTime == 0) for(r = 6; 0 <= r; r--) for(c = 0; c < 7; c++) if(board[r * 7 + c] == 0 || (board[r * 7 + c] & fallEffectBit) != 0){
			board[r * 7 + c] = 0 < r ? board[(r - 1) * 7 + c] : newblock();
			board[r * 7 + c] |= fallEffectBit;
			if(0 < r)
				board[(r - 1) * 7 + c] = 0;
			fallTime = fallMaxTime;
		}

	}

	public void check(){
		int r, c;

		if(eraseEffectTime != 0 || fallTime != 0 || rollTime != 0)
			return;

		eraseFrame = 0;

		// horizontal
		for(r = 0; r < 7; r++) for(c = 0; c < 7 - 3; c++){
			int n, i, cc = 1;
			int src = board[r * 7 + c] & ~(eraseEffectBit);
			if(src == 0 || src == 1)
				continue;
			for(i = 1; c + i < 7; i++){
				int target = board[r * 7 + (c + i)];
				if((target & ~eraseEffectBit) != src)
					break;
				else if(0 == (target & eraseEffectBit))
					cc++;
			}
			if(i < 4 || cc == 1)
				continue;
			for(int j = 0; j < i; j++)
				board[r * 7 + (c + j)] |= eraseEffectBit;
/*			for(n = r; 0 <= n; n--) for(int j = 0; j < i; j++){
				if(n != 0)
					board[n * 7 + (c + j)] = board[(n - 1) * 7 + (c + j)] | (n == r ? eraseEffectBit : 0);
				else
					board[n * 7 + (c + j)] = newblock();
				board[n * 7 + (c + j)] |= (n == r ? eraseEffectBit : 0);
			}*/
			scorePoint(cc, r * cellsize, c * cellsize + i * cellsize / 2);
		}

		// vertical
		for(r = 0; r < 7 - 3; r++) for(c = 0; c < 7; c++){
			int n, i, cc = 1;
			int src = board[r * 7 + c] & ~(eraseEffectBit);
			if(src == 0 || src == 1)
				continue;
			for(i = 1; r + i < 7; i++){
				int target = board[(r + i) * 7 + c];
				if((target & ~eraseEffectBit) != src)
					break;
				else if(0 == (target & eraseEffectBit))
					cc++;
			}
			if(i < 4 || cc == 1)
				continue;
			for(int j = 0; j < i; j++)
				board[(r + j) * 7 + c] |= eraseEffectBit;
/*			for(n = r + i - 1; 0 <= n; n--){
				if(i < n)
					board[n * 7 + c] = board[(n - i) * 7 + c];
				else
					board[n * 7 + c] = newblock();
				board[n * 7 + c] |= (r <= n ? eraseEffectBit : 0);
			}*/
			scorePoint(cc, r * cellsize + i * cellsize / 2, c * cellsize);
		}

		// rightside-down diagonal
		for(r = 0; r < 7 - 3; r++) for(c = 0; c < 7 - 3; c++){
			int n, i, cc = 1;
			int src = board[r * 7 + c] & ~(eraseEffectBit);
			if(src == 0 || src == 1)
				continue;
			for(i = 1; c + i < 7 && r + i < 7; i++){
				int target = board[(r + i) * 7 + (c + i)];
				if((target & ~eraseEffectBit) != src)
					break;
				else if(0 == (target & eraseEffectBit))
					cc++;
			}
			if(i < 4 || cc == 1)
				continue;
			for(int j = 0; j < i; j++)
				board[(r + j) * 7 + (c + j)] |= eraseEffectBit;
/*			for(int j = 0; j < i; j++) for(n = r + j; 0 <= n; n--) if(n != 0)
				board[n * 7 + (c + j)] = board[(n - 1) * 7 + (c + j)] | (n == r + j ? eraseEffectBit : 0);
			else
				board[n * 7 + (c + j)] = newblock();*/
			scorePoint(cc, r * cellsize + i * cellsize / 2, c * cellsize + i * cellsize / 2);
		}

		// rightside-up diagonal
		for(c = 0; c < 7 - 3; c++) for(r = 3; r < 7; r++){
			int n, i, cc = 1;
			int src = board[r * 7 + c] & ~(eraseEffectBit);
			if(src == 0 || src == 1)
				continue;
			for(i = 1; c + i < 7 && 0 <= r - i; i++){
				int target = board[(r - i) * 7 + (c + i)];
				if((target & ~eraseEffectBit) != src)
					break;
				else if(0 == (target & eraseEffectBit))
					cc++;
			}
			if(i < 4 || cc == 1)
				continue;
			for(int j = 0; j < i; j++)
				board[(r - j) * 7 + (c + j)] |= eraseEffectBit;
/*			for(int j = 0; j < i; j++) for(n = r - j; 0 <= n; n--) if(n != 0)
				board[n * 7 + (c + j)] = board[(n - 1) * 7 + (c + j)] | (n == r - j ? eraseEffectBit : 0);
			else
				board[n * 7 + (c + j)] = newblock();*/
			scorePoint(cc, r * cellsize - i * cellsize / 2, c * cellsize + i * cellsize / 2);
		}

	}

    public String getAppletInfo() {
	return "Kashiwamochi game";
    }
}
