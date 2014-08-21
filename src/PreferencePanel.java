import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.naming.Context;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class PreferencePanel{
	private final static String FILE_NAME = "pref.dat";

	public static Preference pref;

	static JFrame prefFrame;
	static JPanel topPanel;
	static JPanel centerPanel;
	static JPanel bottomPanel;
	static JTextField jsonPathField;
	static JTextField servAddrField;
	static JButton saveBtn;
	
	PreferencePanel(){
		pref = new Preference();
		pref.jsonPath = "/Users/NishiokaTeturou/Dropbox/DeadlineGCal-5ffdecf38ca6.p12";
		pref.servAddr = "410339112158-18h2rtkttrnst2oq3seohj4eiopha0pj@developer.gserviceaccount.com";
	
		prefFrame = new JFrame();
		jsonPathField = new JTextField(10);
		servAddrField = new JTextField(10);
		saveBtn = new JButton("Save");
		saveBtn.setActionCommand("SaveBtn");
		
		centerPanel = new JPanel();
		centerPanel.add(jsonPathField);
		centerPanel.add(servAddrField);
		bottomPanel = new JPanel();
		bottomPanel.add(saveBtn,BorderLayout.CENTER);
		
//		prefFrame.add(topPanel,BorderLayout.NORTH);
		prefFrame.add(centerPanel,BorderLayout.CENTER);
		prefFrame.add(bottomPanel,BorderLayout.SOUTH);
	}
	
	public void showFrame(){
		jsonPathField.setText(pref.jsonPath);
		servAddrField.setText(pref.servAddr);
		prefFrame.setBounds(AppFrame.mainFrame.bounds().x+10,AppFrame.mainFrame.bounds().y+10,300,150);
		prefFrame.setVisible(true);
	}
	
	public static class prefFrameActionListener implements ActionListener{
		public  void actionPerformed(ActionEvent e){
			switch(e.getActionCommand()){
			case "SaveBtn":
				saveToFile();
				break;
			case "calBox":
				break;	
			}	
		}
	}
	
	public static void saveToFile(){
		pref.appFrameBound = AppFrame.mainFrame.bounds();
		
		try{
			FileOutputStream fos = new FileOutputStream(FILE_NAME);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(pref);
			oos.close();
		}catch(Exception exc){
			System.out.println("Failed to save preferences");
		}
	}
	
	public Preference readFromFile(){
		try{
			FileInputStream fis = new FileInputStream(FILE_NAME);
			ObjectInputStream ois = new ObjectInputStream(fis);
			pref = (Preference)ois.readObject();
			ois.close();
			return pref;
		}catch(Exception exc){
			System.out.println("Failed to read preferences");
		}
		
		return null;
	}
	
	public class Preference implements Serializable{
		public String jsonPath;
		public String servAddr;
		public Rectangle appFrameBound;
		
		/*Constructor (set initial value)*/
		Preference(){
			this.jsonPath = "/Users/NishiokaTeturou/Dropbox/DeadlineGCal-5ffdecf38ca6.p12";
			this.servAddr = "410339112158-18h2rtkttrnst2oq3seohj4eiopha0pj@developer.gserviceaccount.com";
			appFrameBound = new Rectangle(10, 10, 480, 380);
		}
	}
}

