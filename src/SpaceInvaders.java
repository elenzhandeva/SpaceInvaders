/* ELENA ZHAN
 * Mini Game - Space Invaders
 * DUE: Feb 10th, 2015
 * 
 * A re-make of the arcade game Space Invaders (with a Harry Potter theme)
 * 	- player moves around horizontally on the bottom of the screen using arrow keys, shoots with space bar
 * 	- aliens (Dracos) move horizontally across the top of the screen as a group and make their way downwards a bit each time one hits a wall
 * 	- a big alien (Snape) walks across the very top of the screen ocassionally. He is worth an unknown amount of points (random each time)
 * 	- there are 3 barriers between the aliens and the player lined up near the bottom of the screen.
 * 			These are made up of bricks which can each withstand 3 bullets (from player or alien) changing from blue>green>red before disappearing
 * 			If the aliens make their way down to the barrier, game over
 * 	- each time player clears a round of aliens, the speed of the alien's bullets will increase
 * 			the number of total alien bullets on the screen at a time will also increase
 * 			the player's bullet speed and count will always remain the same
 * 	- player has 3 lives (Harry, Ron, Hermione) and when they are all used up, game over as well
 * 	- a text file keeps track of all the scores and is used to display the top scores
 * 	- scoring is based on number of aliens killed and number of times a round of aliens are cleared as well as the big alien
 */




import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.Timer;
import javax.swing.*;

public class SpaceInvaders extends JFrame implements ActionListener{
	
	private static String whichpanel;	//keeps track of the main screen. used to separate what should be drawn on the Panel
	
	GamePanel game;
	Timer myTimer;
	
	public static void main (String[] args){
		new SpaceInvaders();
	}
	
	public SpaceInvaders(){
		super ("House Invaders! Protect Gryffindor Tower!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500,800);

		game = new GamePanel();
		myTimer = new Timer(20,this);
		myTimer.start();

		whichpanel = "gamepanel";		//starts with the game screen
		
		add(game);
		setResizable(false);
		setVisible(true);		
	}
	
	public static void setPanel(String setStr){		//these methods allow access to variable which panel to be used in paintcomponent
		whichpanel = setStr;
	}
	public static String getPanel(){
		return whichpanel;
	}
	
	public void actionPerformed(ActionEvent evt){
		Object source = evt.getSource();
		if (whichpanel.equals("gamepanel")){
			game.requestFocus();
			game.moveAliens();		//Aliens move
			game.shootAlien();		//Aliens shoot
			game.movePlayer();		//Player moves
			game.shootPlayer();		//Player shoots
			game.moveBullets();		//all bullets on screen move
			game.checkShots();		//check if any bullet hits something
			game.checkAliens();		//check if there are still any Aliens alive
			game.checkbigAlien();	//if conditions for the big alien to appear are met, chance decides whether it will appear
			if (game.bigAexists()==true){	//if big alien is present on screen, move it
				game.movebigAlien();
			}
			game.button();		//pause button
			game.repaint();
		}
		
		if (whichpanel.equals("pausepanel")){
			game.button();		//play button allows player to resume game
			game.repaint();
		}
		
		else if (whichpanel.equals("endpanel")){
			game.entername();	//user enters a name for highscores
			game.button();		//quit button
			game.repaint();
		}
	}
	
}


class GamePanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener{
	private boolean[] keys, mouse;		//list of keys/buttons clicked
	private boolean keypressed, canclick;			//used for entering name, keeps track of when key is pressed so chrs don't repeat from holding down
	private char inchar;		//keeps track of last char entered on keyboard
	private int mx, my;			//mouse x,y coords relative to panel
	
	private int sizeAx, sizeAy, dirAx, dirAy;	//size and directions of aliens (all the same)
	private ArrayList<Integer> posAxes = new ArrayList<Integer>();		//keeps track of positions of each alien still alive
	private ArrayList<Integer> posAyes = new ArrayList<Integer>();
	
	private int sizePx, sizePy, posPx, posPy, dirPx, dirPy;				//size, position, direction of player
	private int sizePBx, sizePBy, sizeABx, sizeABy, dirPBy, dirABy;		//x,y dimensions and directions/speed of bullets
	private ArrayList<Integer> posPBxes = new ArrayList<Integer>();		//positions of bullets
	private ArrayList<Integer> posPByes = new ArrayList<Integer>();
	private ArrayList<Integer> posABxes = new ArrayList<Integer>();
	private ArrayList<Integer> posAByes = new ArrayList<Integer>();
	
	private Image[] aPicsR, aPicsL, pPicsL, pPicsR, greenPics, redPics, lifePics, bigApicsR, bigApicsL;	//contains images
	private Image[] ronPicsL, ronPicsR, herPicsL, herPicsR;
	private Image[][] backupPics;		//chibil pics of ron and hermione that are lives for the player
	private Image BGpic, aPic, pPic, baPic;		//current images used
	private int aPcount, pPcount, tPcount, baPcount;	//sprite counters (small aliens, player, timer for player, big alien)
	private int timecount;		//timer this class used for sprites
	
	private int limABullets, playerbulletlim;	//limit of bullets
	private double difficulty;
	private int score, bigApoints, posBigAx, dirBigAx;		//player's score, random points the big alien is worth, pos and dir of big alien
	
	private int[] posBarx, posBary, statBarriers;		//placement and status of bricks for barrier
	private int sizeBarx, sizeBary;			//dimensions of the bricks

	private boolean canshoot, bigA;	//canshoot=>player can shoot (1 bullet at a time), bigA=>big alien is on screen 
	private int lives;		//# of lives player has (countdown from 3)
	private int posButtonx, posButtony, sizeButtonx, sizeButtony;		//pos and size of play/pause button
	private Image pauseB, playB, exitB, bannerpause, bannerend;			//play/pause buttons and screen images
	private String playername;				//will contain player's inputted name
	private ArrayList<String> Highnames = new ArrayList<String>();		//Names from scores record
	private ArrayList<Integer> Highscores = new ArrayList<Integer>();	//Scores from scores records
	private int[] topscores;		//top 5 scores and names from the records
	private String[] topnames;
	
	public GamePanel(){

		BGpic = new ImageIcon("Pics/HogBG1.png").getImage();
		
		aPicsR = new Image[4];
		aPicsL = new Image[4];
		pPicsL = new Image[4];
		pPicsR = new Image[4];
		ronPicsL = new Image[4];
		ronPicsR = new Image[4];
		herPicsL = new Image[4];
		herPicsR = new Image[4];
		bigApicsL = new Image[4];
		bigApicsR = new Image[4];

		for (int i=0; i<aPicsR.length; i++){
			int num = i+1;
			aPicsR[i] = new ImageIcon("Pics/DracoR"+num+".png").getImage();
			aPicsL[i] = new ImageIcon("Pics/DracoL"+num+".png").getImage();
			pPicsL[i] = new ImageIcon("Pics/HarryL"+num+".png").getImage();
			pPicsR[i] = new ImageIcon("Pics/HarryR"+num+".png").getImage();

			ronPicsL[i] = new ImageIcon("Pics/RonL"+num+".png").getImage();
			ronPicsR[i] = new ImageIcon("Pics/RonR"+num+".png").getImage();
			herPicsL[i] = new ImageIcon("Pics/HermL"+num+".png").getImage();
			herPicsR[i] = new ImageIcon("Pics/HermR"+num+".png").getImage();
			
			bigApicsL[i] = new ImageIcon("Pics/SnapeL"+num+".png").getImage();
			bigApicsR[i] = new ImageIcon("Pics/SnapeR"+num+".png").getImage();
		}

		backupPics = new Image[4][4];
		backupPics[0] = herPicsL;
		backupPics[1] = herPicsR;
		backupPics[2] = ronPicsL;
		backupPics[3] = ronPicsR;
		
		greenPics = new Image[4];	//alien's bullets
		redPics = new Image[4];		//player's bullets
		for (int g=0; g<greenPics.length; g++){
			int num = g+1;
			greenPics[g] = new ImageIcon("Pics/AttackG"+num+".png").getImage();
			redPics[g] = new ImageIcon("Pics/AttackR"+num+".png").getImage();
		}
		
		lifePics = new Image[2];
		lifePics[0] = new ImageIcon("Pics/Hermione.png").getImage();
		lifePics[1] = new ImageIcon("Pics/Ron.png").getImage();

		bannerpause = new ImageIcon("Pics/bannerpause.png").getImage();
		bannerend = new ImageIcon("Pics/bannerend.png").getImage();
		pauseB = new ImageIcon("Pics/pause.png").getImage();
		playB = new ImageIcon("Pics/play.png").getImage();
		exitB = new ImageIcon("Pics/exit.png").getImage();
		posButtonx = 450;
		posButtony = 700;
		sizeButtonx = pauseB.getWidth(this);
		sizeButtony = pauseB.getHeight(this);
		

		
		playerbulletlim = 5;
		bigA=false;
		
		sizeBarx=30;
		sizeBary=10;
		
		//posAx = 0;
		//posAy = 0;
		sizeAx = aPicsR[0].getWidth(this);
		sizeAy = aPicsR[0].getHeight(this);
		dirAx = 1;
		dirAy = 20;
		
		sizePx = 50;
		sizePy = 50;
		posPx = 0;
		posPy = 650;
		
		
		dirPBy = -5;
		sizePBx = 3;
		sizePBy = 5;
		
		dirABy = 5;
		sizeABx = 3;
		sizeABy = 5;
		limABullets= 2;
		lives = 2;

		difficulty = .02;
		score = 0;
		playername="";
		timecount = 0;
		topscores = new int[5];
		topnames = new String[5];
		
		addAliens();
		addBarriers();
		readscores();
		
		keys = new boolean[KeyEvent.KEY_LAST+1];
		mouse= new boolean[MouseEvent.MOUSE_LAST+1];
		keypressed = false;
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);		//focus on mac is not on panel. (keys won't work w/out focus)
        requestFocus();
	}

	
	public void addAliens(){	//set positions of Aliens
		posAyes.add(0);
		posAyes.add(0);		
		posAyes.add(0);
		
		posAyes.add(50);
		posAyes.add(50);
		
		posAyes.add(100);
		posAyes.add(100);
		posAyes.add(100);
		
		posAxes.add(0);
		posAxes.add(100);
		posAxes.add(200);
		
		posAxes.add(50);
		posAxes.add(150);
		
		posAxes.add(0);
		posAxes.add(100);
		posAxes.add(200);		
	}
	
	public void addBarriers(){		//set positions and status of barrier bricks
		posBarx = new int[] {50,80,110, 190,220,250, 330,360,390,
								65,95, 205,235, 345,375,
								80,220,360};
		posBary = new int[] {600,600,600, 600,600,600, 600,600,600,
								610,610,610,610,610,610,
								620,620,620};
		statBarriers = new int[18];
		for (int i=0; i<statBarriers.length; i++){
			statBarriers[i]=3;		//3>most intact  to 0>gone
		}
	}
	
	public void moveAliens(){
		int leftA = 10000;		//find the left and rightmost coordinates to use when changing direction (hitting wall)
		int rightA = -10000;
		for (int i=0; i<posAxes.size(); i++){
			int newAx = posAxes.get(i)+dirAx;
			posAxes.set(i, newAx);
			leftA = Math.min(newAx, leftA);
			rightA = Math.max(newAx, rightA);
		}
		
		timecount+=1;	//since aliens move at a constant speed, put timer here.
		
		if (timecount%15==0){		//depending of direction alien is headed, us different list of pictures for L/R
			if (dirAx>0){
				walkAlien(aPicsR);
			}
			else{
				walkAlien(aPicsL);
			}
		}
		if (leftA<=0 || rightA>=getWidth()-aPicsR[0].getWidth(this)){	//if hit wall, change direction and shift downwards
			dirAx *=(-1);
			for (int i=0; i<posAyes.size(); i++){
				int newAy = posAyes.get(i)+dirAy;
				posAyes.set(i, newAy);
			}
		}
		if (posAyes.get(posAyes.size()-1) >=600-sizeAy){		//if aliens reach barrier level, game over
			SpaceInvaders.setPanel("endpanel");
		}
	}
	
	public void walkAlien(Image[] piclist){	//takes pic list of alien and changes the sprites
		aPic = piclist[aPcount];
		aPcount+=1;
		if(aPcount<0 || aPcount >3){
			aPcount=0;
		}
		sizeAx = aPic.getWidth(this);
		sizeAy = aPic.getHeight(this);
	}
	
	public void shootAlien(){		//aliens shoot randomly, many bullets can be on the screen at once
		if (posABxes.size()<limABullets && Math.random()<difficulty){
			int n = (int)(Math.random()*posAxes.size());
			posABxes.add(posAxes.get(n)+sizeAx/2);
			posAByes.add(posAyes.get(n));
		}
	}
	
	public void movePlayer(){		//move player arround according to arrowkeys pressed
		tPcount+=1;
		if(keys[KeyEvent.VK_RIGHT]){
			posPx += 5;		//change xcoorinate towards R
			posPx = Math.min(getWidth()-sizePx, posPx);	//don't allow it past the wall
			if (tPcount%10==0){		//sprites work
				walkPlayer(pPcount, pPicsR);
			}
		}
		else if(keys[KeyEvent.VK_LEFT] ){
			posPx -= 5;
			posPx = Math.max(0,posPx);
			
			if (tPcount%10==0){
				walkPlayer(pPcount, pPicsL);
			}
		}
	}
	
	public void walkPlayer(int pCount, Image[] piclist){
		pPic = piclist[pPcount];
		pPcount+=1;
		if(pPcount<0 || pPcount >3){
			pPcount=0;
		}
		sizePx = pPic.getWidth(this);
		sizePy = pPic.getHeight(this);
	}

	public void shootPlayer(){		//player can only shoot 1 bullet at a time (playerbulletlim=1) according to space key
		if(keys[KeyEvent.VK_SPACE] && canshoot){
			posPBxes.add(posPx+sizePx/2-sizePBx/2);
			posPByes.add(posPy);
			canshoot=false;
		}
		if(keys[KeyEvent.VK_SPACE]==false){
			canshoot=true;
		}
		if(posPBxes.size()>playerbulletlim-1){
			canshoot=false;
		}
	}

	
	public void moveBullets(){	//moves all the bullets on the screen
		for (int i=0; i<posPByes.size(); i++){
			int newPBy = posPByes.get(i)+dirPBy;
			if(newPBy<0){
				posPByes.remove(i);		//if new cood is off screen, remove it
				posPBxes.remove(i);
			}
			else{
				posPByes.set(i, newPBy);
			}
		}
		for (int i=0; i<posAByes.size(); i++){
			int newABy = posAByes.get(i)+dirABy;
			if(newABy>getHeight()){
				posAByes.remove(i);
				posABxes.remove(i);
			}
			else{
				posAByes.set(i, newABy);
			}
		}
	}
	
	public void checkShots(){
		if (posPBxes.size()>0 && posAxes.size()>0){		//Checks players bullets against aliens
			int bx1=posPBxes.get(0);	//player's bullets' 4 corners
			int bx2=bx1+sizePBx;
			int by1=posPByes.get(0);
			int by2=by1+sizePBy;
			for (int a=0; a<posAxes.size(); a++){	//for every alien
				int ax1=posAxes.get(a);		//alien's 4 corners
				int ax2=ax1+sizeAx;
				int ay1=posAyes.get(a);
				int ay2=ay1+sizeAy;
				if (bx1<=ax2 && bx2>=ax1 && by1<ay2 && by2>=ay1){	//check each pair of bullets (since bullets so much smaller than aliens, don't worry about overlapping issues
					posAxes.remove(a);		//if hit, remove alien and bullet from respective lists
					posAyes.remove(a);
					posPBxes.remove(0);
					posPByes.remove(0);
					score+= (int)25*dirABy;		//add points
				}
			}
		}
		if (posABxes.size()>0){		//Checks aliens' bullets against player
			int px1=posPx;
			int px2=px1+sizePx;
			int py1=posPy;
			int py2=py1+sizePy;
			for (int a=0; a<posABxes.size(); a++){
				int ax1=posABxes.get(a);
				int ax2=ax1+sizeABx;
				int ay1=posAByes.get(a);
				int ay2=ay1+sizeABy;
				
				if (px1<=ax2 && px2>=ax1 && py1<ay2 && py2>=ay1){	//check if it hits player
					if(lives<=0){		// if player out of lives, game over
						SpaceInvaders.setPanel("endpanel");
					}
					else{		//otherwise, remove a life ad continues
						lives -=1;
						posPx = 0;
						pPicsL = backupPics[(lives)*2];	//take next person's picture lists (ron or hermione)
						pPicsR = backupPics[(lives)*2+1];
						posABxes.remove(a);		//clear old bullets off screen)
						posAByes.remove(a);
					}
				}
			}
		}
		if (posABxes.size()>0){		//Checks alien bullets against barriers
			for (int a=0; a<posABxes.size(); a++){
				boolean looping = true;			//nested for loop, removing a in second loop messes up size (out of range)
				int ax1=posABxes.get(a);			// "looping" acts as a "break" command
				int ax2=ax1+sizeABx;
				int ay1=posAByes.get(a);
				int ay2=ay1+sizeABy;
				
				for (int b=0; b<statBarriers.length; b++){
					int bx1=posBarx[b];
					int bx2= bx1+sizeBarx;
					int by1=posBary[b];
					int by2= by1+sizeBary;
					if (bx1<=ax2 && bx2>=ax1 && by1<ay2 && by2>=ay1 && statBarriers[b]>0 && looping==true){
						statBarriers[b]-=1;		//destroy barrier a little
						posABxes.remove(a);		//and remove alien bullet from screen.
						posAByes.remove(a);
						looping=false;
					}
				}
			}
			
			for (int p=0; p<posPBxes.size(); p++){		//checks player's bullets against barriers
				boolean looping = true;	
				int px1=posPBxes.get(p);
				int px2=px1+sizePBx;
				int py1=posPByes.get(p);
				int py2=py1+sizePBy;
				
				for (int b=0; b<statBarriers.length; b++){
					int bx1=posBarx[b];
					int bx2= bx1+sizeBarx;
					int by1=posBary[b];
					int by2= by1+sizeBary;
					if (bx1<=px2 && bx2>=px1 && by1<py2 && by2>=py1 && statBarriers[b]>0 && looping==true){
						statBarriers[b]-=1;
						posPBxes.remove(p);
						posPByes.remove(p);
						looping=false;
					}
				}
			}
			
			if (bigA && posPBxes.size()>0){			//if the big alien's on the screen, and player has shot bullets
				for (int p=0; p<posPBxes.size(); p++){		//check each pain to see if they hit and dissapear.
					int px1=posPBxes.get(p);
					int px2=px1+sizePBx;
					int py1=posPByes.get(p);
					int py2=py1+sizePBy;
					
					int bx1=posBigAx;
					int bx2= bx1+baPic.getWidth(this);
					int by1=0;
					int by2= baPic.getHeight(this);
					if (bx1<=px2 && bx2>=px1 && by1<py2 && by2>=py1){
						score+=bigApoints;		//if hit, remove big Aplien from screen
						bigA = false;
					}
				}
			}
		}
	}
	
	public void checkAliens(){		//if all aliens are killed, make game harder (alien bullets faster & more in quantity)
		if(posAxes.size()<=0){
			posPBxes.clear();
			posPByes.clear();
			posABxes.clear();
			posAByes.clear();
			addAliens();		//reset aliens
			//difficulty *=2;
			limABullets +=2;
			dirABy *=1.2;
			if (dirAx<0){
				dirAx*=-1;
			}
			score+=150;
		}
		
	}
	
	public boolean checkbigAlien(){		//checks if big alien is elidgable to appear on screen
		double n=Math.random();		//random timing for it to show
		if (posAyes.get(0)>100 && score>150 && n<0.001 && bigA==false){ //based on change, where aliens and students are
			bigApoints = (int)(n*1000);
			bigA = true;
			createbigAlien();
			return true;
		}
		else{
			return false;
		}
	}
	public void createbigAlien(){	//initialize/reset big alien stats
		baPcount = 1;
		int n= (int)Math.random();
		if (n<0.5){
			posBigAx = 0;
			dirBigAx = 3;
		}
		else{
			posBigAx = getWidth();
			dirBigAx = -3;
		}		
	}
	public void movebigAlien(){		//moves big alien just like the small ones
		posBigAx += dirBigAx;
		if (timecount%15==0){
			if (dirBigAx>0){
				walkbigAlien(bigApicsR);
			}
			else{
				walkbigAlien(bigApicsL);
			}
		}
		if (posBigAx<0 || posBigAx>getWidth()-bigApicsL[0].getWidth(this)){
			bigA = false;		//big Alien is now off screen
		}
	}	
	public void walkbigAlien(Image[] piclist){		//big alien's sprites
		baPic = piclist[baPcount];
		baPcount+=1;
		if(baPcount<0 || baPcount >3){
			baPcount=0;
		}
	}
	public boolean bigAexists(){	//allows other SpaceInvaders class to know if there is a big alien on screen
		return bigA;
	}
	
// _____________________NECESSARY FOR LISTENERS_______________________
    public void keyTyped(KeyEvent e) {
    	inchar = e.getKeyChar();
    	System.out.println(inchar);
    }
    public void keyPressed(KeyEvent e) {
    	keypressed=true;
        keys[e.getKeyCode()] = true;
    }
    public void keyReleased(KeyEvent e) {
    	keypressed=false;
        keys[e.getKeyCode()] = false;
    }
	public void mouseClicked(MouseEvent m){}
	public void mouseEntered(MouseEvent m){}
	public void mouseExited(MouseEvent m){}
	public void mousePressed(MouseEvent m){
		mouse[m.getButton()] = true;
	}
	public void mouseReleased(MouseEvent m){
		mouse[m.getButton()] = false;
	}
	public void mouseMoved(MouseEvent m){
		mx = m.getX();
		my = m.getY();
	}
	public void mouseDragged(MouseEvent m){}
//_________________________________________________________________
	
	public void button(){		//if button is clicked change panel according to current panel
		if (mouse[MouseEvent.BUTTON1] && canclick && posButtonx<mx && mx<posButtonx+sizeButtonx && posButtony<my && my<posButtony+sizeButtony){
			if (SpaceInvaders.getPanel().equals("gamepanel")){
				SpaceInvaders.setPanel("pausepanel");
			}
			else if (SpaceInvaders.getPanel().equals("pausepanel")){
				SpaceInvaders.setPanel("gamepanel");
			}
			else if (SpaceInvaders.getPanel().equals("endpanel")){
				addscore();
				System.exit(0);
			}
			canclick=false;
		}
		if(mouse[MouseEvent.BUTTON1]==false){
			canclick=true;
		}
	}
	
 	public void readscores(){		//reads scores from file into arraylists of scores and names
		Scanner infile = null;
		try{
			infile = new Scanner(new File("Highscores.txt"));
		}
		catch(IOException ex){
			System.out.println("Oops, where did I put that file?");
		}
		while(infile.hasNextLine()){
			String line = infile.nextLine();
			int j = line.indexOf(" - ");
			String name = line.substring(0, j);
			int score = Integer.parseInt(line.substring(j+3));
			
			Highnames.add(name);
			Highscores.add(score);
		}
		gettopscores();
 	}

 	public void gettopscores(){		//get the top 5 scores and names associated with them
 		for (int i=0; i<topscores.length;i++){
 			if (Highscores.size()>0){
 				topscores[i] = -10000;
 				for (int j=0; j<Highscores.size();j++){
 					if(Highscores.get(j)>topscores[i]){
 						topscores[i] = Highscores.get(j);
 					}
 				}
 				int n=Highscores.indexOf(topscores[i]);
 				topnames[i] = Highnames.get(n);
 				Highscores.remove(n);
 				Highnames.remove(n);
 			}
 		}
 	}
 	
 	public void addscore(){		//outputs score into record of highscores
 		try{
 			FileWriter fw = new FileWriter("Highscores.txt", true);		//true => appends to file without deleting
 			playername.trim();
 			fw.write("\n"+playername+" - "+score);		//reads as lines therefore must have "\n"
 			fw.close();
 		}
 		catch(IOException ex){
 			System.out.println("ERROR occurred.");
 		}
 	}
 	
 	public void entername(){	//add to player name when inputing in end screen, chr at a time
 		if (keypressed){
 			playername = playername+ inchar;
 			keypressed = false;		//does not allow holded keys to repeat (loops too quickly)
 		}
 	}
 	
	@Override
	public void paintComponent(Graphics g){
		g.setColor(new Color(220,220,220));
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(BGpic, 0, 0, getWidth(), getHeight(),this);
		
		for (int i=0; i<posAxes.size(); i++){	//DRAW ALIENS
			g.drawImage(aPic,posAxes.get(i), posAyes.get(i), this);
		}
		
		g.drawImage(pPic,posPx, posPy, this);	//DRAW PLAYER
		
		for (int i=0; i<posPBxes.size(); i++){ //PLAYER BULLETS
			g.drawImage(redPics[0],posPBxes.get(i), posPByes.get(i), this);
		}
		
		for (int i=0; i<posABxes.size(); i++){ //ALIEN BULLETS
			g.drawImage(greenPics[0],posABxes.get(i), posAByes.get(i), this);
		}

		for (int i=0; i<statBarriers.length; i++){		//draw barriers, colour coded for status of destruction
			if (statBarriers[i]==3){
				g.setColor(Color.blue);
				g.fillRect(posBarx[i],posBary[i], sizeBarx, sizeBary);
				g.setColor(Color.black);
				g.drawRect(posBarx[i],posBary[i], sizeBarx, sizeBary);
			}
			else if (statBarriers[i]==2){
				g.setColor(Color.green);
				g.fillRect(posBarx[i],posBary[i], sizeBarx, sizeBary);
				g.setColor(Color.black);
				g.drawRect(posBarx[i],posBary[i], sizeBarx, sizeBary);
			}
			else if (statBarriers[i]==1){
				g.setColor(Color.red);
				g.fillRect(posBarx[i],posBary[i], sizeBarx, sizeBary);
				g.setColor(Color.black);
				g.drawRect(posBarx[i],posBary[i], sizeBarx, sizeBary);
			}
		}
		
		if (bigA){		//if big alien is on screen, draw him too.
			g.drawImage(baPic, posBigAx, 0, this);
		}
		
		if (lives>0){		//draw Herm and Ron in corner as lives
			for(int i=0; i<lives; i++){
				g.drawImage(lifePics[i], i*70, 700, this);
			}
		}
		g.drawImage(pauseB, posButtonx, posButtony, this);	//draws pause button in corner
		g.setColor(Color.white);
		g.drawString("SCORE: "+Integer.toString(score),300,750);
		
		if (SpaceInvaders.getPanel().equals("pausepanel")){
			g.drawImage(bannerpause, 50, 0, this);
			g.setColor(Color.red);
			g.drawString("YOUR SCORE: "+Integer.toString(score),200, 200);
			g.drawString("TOP SCORES",210, 250);
			for (int i=0; i<topnames.length;i++){
				g.drawString(topnames[i], 180,280+i*30);
				g.drawString(Integer.toString(topscores[i]),280,280+i*30);
			}
			g.drawImage(playB, posButtonx, posButtony, this);
		}
		
		if (SpaceInvaders.getPanel().equals("endpanel")){
			g.drawImage(bannerend, 50, 0, this);
			g.setColor(Color.red);
			g.drawString("YOUR SCORE: "+Integer.toString(score),200, 200);
			g.drawString("TOP SCORES",210, 250);
			for (int i=0; i<topnames.length;i++){
				g.drawString(topnames[i], 180,280+i*30);
				g.drawString(Integer.toString(topscores[i]),280,280+i*30);
			}
			g.setColor(Color.black);
			g.fillRect(100, 150, 300, 30);
			g.setColor(Color.white);
			g.drawString("Please type your name:", 105, 135);
			g.drawString(playername, 105, 155);
			g.drawImage(exitB, posButtonx, posButtony, this);
		}
	}
}
