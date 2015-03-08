import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.naming.Context;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class PreferencePanel implements Serializable{
	private final static String FILE_NAME = "pref.dat";

	public static Preference pref;

	static JFrame prefFrame;
	static JPanel topPanel;
	static JPanel bottomPanel;
	static JLabel jsonPathFieldLabel;
	static JLabel servAddrFieldLabel;
	static JTextField jsonPathField;
	static JTextField servAddrField;
	static JLabel statusLabel;
	static JButton saveBtn;
//	static JComboBox<String> calBox;

	
	PreferencePanel(){
		pref = readFromFile();
		if(pref==null){
			System.out.println("Failed to read preferences");
			pref = new Preference();
			pref.jsonPath = "/Users/tetsurou/Dropbox/Accounts/DeadlineGCal-5ffdecf38ca6.p12";
			pref.servAddr = "410339112158-18h2rtkttrnst2oq3seohj4eiopha0pj@developer.gserviceaccount.com";
		}else{
			System.out.println("Read preference");
		}
	
		prefFrame = new JFrame();
		GroupLayout gLayout = new GroupLayout(prefFrame.getContentPane());
		prefFrame.setLayout(gLayout);
		prefFrame.setResizable(false);
		
		/*Initialize components*/
		final int row = 2;
		
		/*Set up each field*/
		JLabel[] labels = new JLabel[row];			//Label and textField should line with same order
		JTextField[] textFields = new JTextField[row];	//Same above
		//json path field 
		jsonPathFieldLabel = new JLabel(" JsonFilePath ");
		jsonPathField = new JTextField(pref.jsonPath.length());
		labels[0] = jsonPathFieldLabel;
		textFields[0] = jsonPathField;
		//server address preferences field
		servAddrFieldLabel = new JLabel(" ServiceAddress ");
		servAddrField = new JTextField(pref.servAddr.length());
		labels[1] = servAddrFieldLabel;
		textFields[1] = servAddrField;
		//operation and result field
		saveBtn = new JButton("Save");
		saveBtn.setActionCommand("SaveBtn");
		saveBtn.addActionListener(new prefFrameActionListener());
		statusLabel = new JLabel("");
		
		//Setup vertical group
		GroupLayout.SequentialGroup vGroup = gLayout.createSequentialGroup();
		for(int i = 0; i <  row;i++){//Setup each horizontal group
			GroupLayout.ParallelGroup group = gLayout.createParallelGroup(Alignment.BASELINE);
			group.addComponent(labels[i]);
			group.addComponent(textFields[i]);
			vGroup.addGroup(group);
		}
		GroupLayout.ParallelGroup group = gLayout.createParallelGroup(Alignment.BASELINE);
		group.addComponent(saveBtn);
		group.addComponent(statusLabel);
		vGroup.addGroup(group);
		
		//Setup horizontal group
		GroupLayout.SequentialGroup hGroup = gLayout.createSequentialGroup();
		GroupLayout.ParallelGroup labelGroup = gLayout.createParallelGroup();
		GroupLayout.ParallelGroup textGroup = gLayout.createParallelGroup();
		for(int i = 0;i < row;i ++){
			labelGroup.addComponent(labels[i]);
			textGroup.addComponent(textFields[i]);
		}
		labelGroup.addComponent(saveBtn);
		textGroup.addComponent(statusLabel);
		hGroup.addGroup(labelGroup);
		hGroup.addGroup(textGroup);

		//Set Group layout
		gLayout.setVerticalGroup(vGroup);
		gLayout.setHorizontalGroup(hGroup);
		
//		topPanel = new JPanel();
//		topPanel.setLayout(gLayout);
//		bottomPanel = new JPanel();
//		bottomPanel.add(saveBtn,BorderLayout.CENTER);
//		bottomPanel.add(statusLabel,BorderLayout.CENTER);
//		
		//コンボボックス
//		calBox = new JComboBox();
//		calBox.setActionCommand("calBox");
//		calBox.setSize(5, 3);
//		calBox.setMinimumSize(new Dimension(4,3));
//		calBox.setMaximumSize(new Dimension(10,3));
//		calBox.addActionListener(new mainFrameActionListener());
		
//		prefFrame.add(topPanel,BorderLayout.NORTH);
//		prefFrame.add(topPanel,BorderLayout.NORTH);
//		prefFrame.add(bottomPanel,BorderLayout.SOUTH);
	}
	
	public void showFrame(){
		jsonPathField.setText(pref.jsonPath);
		servAddrField.setText(pref.servAddr);
		statusLabel.setText("");
		prefFrame.setBounds(AppFrame.mainFrame.bounds().x+20,
							AppFrame.mainFrame.bounds().y+20,
							400,
							115);
		prefFrame.setVisible(true);
	}
	
	public static class prefFrameActionListener implements ActionListener{
		public  void actionPerformed(ActionEvent e){
			switch(e.getActionCommand()){
			case "SaveBtn":
				if(saveToFile()){
					prefFrame.setVisible(false);
					System.out.println("Succeeded to save pref");
				}
				else{
					statusLabel.setText("Failed to save");
					System.out.println("Failed to save pref");
				}
				break;
			}	
		}
	}
	
	public static boolean saveToFile(){
		pref.appFrameBound = AppFrame.mainFrame.bounds();
		
		try{
			FileOutputStream fos = new FileOutputStream(FILE_NAME);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(pref);
			oos.flush();
			oos.close();
		}catch(Exception exc){
			System.out.println("Failed to save preferences");
			exc.printStackTrace();
			return false;
		}
		
		return true;
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
	
	/* Model class */
	public class Preference implements java.io.Serializable{
		public String jsonPath;
		public String servAddr;
		public Rectangle appFrameBound;
		
		/*Constructor (set initial value)*/
		Preference(){
			this.jsonPath = "/Users/tetsurou/Dropbox/Accounts/DeadlineGCal-5ffdecf38ca6.p12";
			this.servAddr = "410339112158-18h2rtkttrnst2oq3seohj4eiopha0pj@developer.gserviceaccount.com";
			appFrameBound = new Rectangle(10, 10, 480, 380);
		}
		
//		public void writeObject(ObjectOutputStream  stream) throws IOException{
//			stream.defaultWriteObject();
//			stream.writeObject(jsonPath);
//			stream.writeObject(servAddr);
//			stream.writeObject(appFrameBound);
//		}
//		
//		public void readObject(ObjectInputStream  stream) throws ClassNotFoundException, IOException{
//			stream.defaultReadObject();
//			jsonPath = (String) stream.readObject();
//			servAddr = (String) stream.readObject();
//			appFrameBound = (Rectangle)stream.readObject();
//		}
	}
}

