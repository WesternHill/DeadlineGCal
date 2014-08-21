import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.util.List;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.client.youtube.YouTubeQuery.Time;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.dublincore.Date;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.data.codesearch.*;



public class AppFrame{
	private static final int rowNum = 3;
	static final int CLKFIELD_H = 30;	//時刻フィールドの高さ
	static final int BTMFIELD_H = 30;	//操作フィールドの高さ
	
	static gCal Gcal = null;
	static Timer periodic = null;
	static Timer minTimer = null;
	static HashMap<Integer,CalendarEvent> calEvents;
	static PreferencePanel PrefPanel;
	
	//GUIコンポーネント
	static JFrame mainFrame;
	static JPanel paneBtm;
	static JPanel topPane;
	static JLabel dateLabel;
	static JLabel clkLabel;
	static JScrollPane scrPane;
//	static JTextField addrField;
//	static JPasswordField passField;
//	static JComboBox<String> calBox;
	static JButton getBtn;
	static JTable tbl;
	static DefaultTableModel tableModel;
	
	
	/**
	 * フレーム初期化
	 */
	public static void main(String[] args) {
		new AppFrame();
	}
	
	/**
	 * フレーム初期化
	 */
	public AppFrame() {
		initFrame();
	}
	
 	public static void initFrame(){
 		mainFrame = new JFrame();
 		
		//フレーム挙動設定
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.addComponentListener(new mainFramePerformer());
 		//パネル追加
 		topPane = new JPanel();
 		topPane.setBackground(Color.white);
		paneBtm = new JPanel();
		
		//テキストラベル
		clkLabel = new JLabel("Getting ready...");
		clkLabel.setFont(new Font("Helvetica",Font.PLAIN,40));

//		//テキストフィールド
//		addrField = new JTextField(10);
//		passField = new JPasswordField(10);
		
		//ボタン配置
		getBtn = new JButton("Regist");
		getBtn.setActionCommand("getBtn");
		getBtn.addActionListener(new mainFrameActionListener());
		
		//コンボボックス
//		calBox = new JComboBox();
//		calBox.setActionCommand("calBox");
//		calBox.setSize(5, 3);
//		calBox.setMinimumSize(new Dimension(4,3));
//		calBox.setMaximumSize(new Dimension(10,3));
//		calBox.addActionListener(new mainFrameActionListener());

		//スクロールパネルとToDoリストテーブル作成
		String[] columnNames = {"ToDo","Deadline","Daysleft"};
		tableModel = new DefaultTableModel(columnNames,0);
		tbl = new JTable(tableModel);
		scrPane = new JScrollPane(tbl);
		
		/*各GUIパーツの設置*/
		//上部パネルの配置
		topPane.add(clkLabel,BorderLayout.CENTER);
		mainFrame.add(topPane,BorderLayout.NORTH);
		//下部パネルの配置
//		paneBtm.add(addrField,BorderLayout.WEST);
//		paneBtm.add(passField,BorderLayout.WEST);
		paneBtm.add(getBtn,BorderLayout.CENTER);
//		paneBtm.add(calBox,BorderLayout.EAST);
		mainFrame.add(paneBtm,BorderLayout.SOUTH);
		//表本体の配置
		mainFrame.add(scrPane,BorderLayout.CENTER);//パネルをフレームに載せる
		mainFrame.setVisible(true);

		//Adjust GUI parts
 		mainFrame.setBounds(10, 10, 480, 380);
 		topPane.setBounds(0,CLKFIELD_H,mainFrame.getWidth(),CLKFIELD_H);
 		paneBtm.setBounds(0,mainFrame.getHeight()-BTMFIELD_H, mainFrame.getWidth(),BTMFIELD_H);
		scrPane.setBounds(0,CLKFIELD_H,mainFrame.getWidth(),mainFrame.getHeight()-CLKFIELD_H-BTMFIELD_H);//位置とサイズ（100,100の位置に640×480のフレーム
		tbl.setBounds(scrPane.getBounds().x,scrPane.getBounds().y,scrPane.getWidth(),scrPane.getHeight());//位置とサイズ（100,100の位置に640×480のフレーム
		
		//Load and Initialize instances
		PrefPanel = new PreferencePanel();
		PreferencePanel.Preference pref = PrefPanel.readFromFile();
		if(pref == null) pref = PrefPanel.pref;
		Gcal = new gCal(pref.jsonPath,pref.servAddr);
		
		reflectClk();
//		reflectGCal();
		reflectGCalEvent();
		
		//タイマースタート
		periodic = new Timer();
		periodic.schedule(new PeriodicTimer(),0,1000);
		Calendar cal = Calendar.getInstance();
		minTimer = new Timer();
		minTimer.schedule(new CalRefleshTimer(),60-cal.get(Calendar.SECOND),1000*60);
	}
 	
	public static void reflectGCal(){
		CalendarList list = Gcal.getCalendarList();
		
		String[] cals = null;
		int calsIdx = 0;
		
		for(CalendarListEntry listEntry : list.getItems()){
			cals[calsIdx]=listEntry.getSummary();
			calsIdx++;
		}

		for(int i = 0;i < cals.length;i++) System.out.println(cals[i]);
//		setItemToCBox(cals);
	}
	
	public static void reflectGCalEvent(){
		if(Gcal == null) return;

		CalendarList list = Gcal.getCalendarList();
		List<Event> events = Gcal.getEventList(list.getItems().get(1).getId());
		if(events==null) return;
		removeAllToDo();

		for(Event event : events){
			System.out.println("Event:"+event.getSummary());
			String deadlineStr = "--";
			String leftTimeStr = "--";
			
			if(event.getStart()!=null && event.getStart().getDateTime() != null){
				long deadline = event.getStart().getDateTime().getValue();
				String[] deadlineUnits = cvrtMilToString(leftMilSec(deadline));
				leftTimeStr = String.format("%s %s日%s時間%s分", deadlineUnits[0],deadlineUnits[1],deadlineUnits[2],deadlineUnits[3]);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(deadline);
				deadlineStr = String.format("%d-%d %d:%02d:%02d",cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND));
			}
			
			Object[] todo = {event.getSummary(),deadlineStr,leftTimeStr};
			addToDo(todo);
		}
	}
	
	/*CalendarEventから残り時間だけを狙って変更できるよう改良予定*/
	public static void refleshLefttime(){
		if(calEvents==null) return;
		
		for(int i = 0;i < tableModel.getRowCount();i++){
			DateTime deadline;
			if(calEvents.get((Integer)i)!=null) deadline = calEvents.get((Integer)i).deadline;
			else continue;
			String[] deadlineUnits = cvrtMilToString(leftMilSec(deadline.getValue()));
			String deadlineStr = String.format("%s %s日%s時間%s分", deadlineUnits[0],deadlineUnits[1],deadlineUnits[2],deadlineUnits[3]);
			tableModel.setValueAt(deadlineStr,i,2);
		}
	}
	
	public static void reflectClk(){
		Calendar cal = Calendar.getInstance();
		String clk = String.format("%d-%d %d:%02d:%02d",cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND));
		if(6 < cal.get(Calendar.HOUR_OF_DAY) && cal.get(Calendar.HOUR_OF_DAY) < 18){
			topPane.setBackground(Color.white);
			clkLabel.setForeground(Color.black);
		}else{
			topPane.setBackground(Color.gray);
			clkLabel.setForeground(Color.white);
		}
		clkLabel.setText(clk);
	}

	public static void removeAllToDo(){
		System.out.println("Remove "+tableModel.getRowCount());
			tableModel.setRowCount(0);
		if(calEvents!=null)calEvents.clear();
	}
	
	public static void addToDo(Object[] clms){
		if(clms.length != rowNum){
			System.out.println("WARNING: The num of ToDo added is NOT same as rowNum");
		}
		tableModel.addRow(clms);
	}

//	public static  void setItemToCBox(String[] args){
//		calBox.removeAllItems();
//		for(int i = 0;i < args.length;i++){
//			calBox.addItem(args[i]);
//		}
//	}

	/**
	 * 残り時間を計算する
	 */
	static long leftMilSec(long deadline){
		long leftMil = deadline - DateTime.now().getValue();
		return leftMil;
	}
	
	/**
	 * ミリ秒を符号，年，日，時間，分に変換する
	 * @param leftMil
	 * @return
	 */
	static String[] cvrtMilToString(long leftMil){
		String code = "+";
		
		if(leftMil < 0){
			code = "-";
			leftMil*=(-1);
		}
		
		final long SEC = 1000;
		final long MIN = SEC*60;
		final long HR = MIN * 60;
		final long DAY = HR*24;
		
		int day = (int)(leftMil / DAY);
		leftMil -= day*DAY;
		int hr  =  (int)(leftMil / HR);
		leftMil -= hr*HR;
		int min =  (int)(leftMil / MIN);

		//符号，年，時間，分
		String[] rslt = {code,Long.toString(day),Long.toString(hr),Long.toString(min)};
		return rslt;
	}
	
	public static class mainFrameActionListener implements ActionListener{
		public  void actionPerformed(ActionEvent e){
			switch(e.getActionCommand()){
			case "getBtn":
				PrefPanel.showFrame();
				break;
			case "calBox":
				reflectGCalEvent();
				break;	
			}	
		}
	}
	
	public static class mainFramePerformer implements ComponentListener{
		@Override
		public void componentResized(ComponentEvent e) {

		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class PeriodicTimer extends TimerTask{
		public void run(){
			reflectClk();
			refleshLefttime();
		}
	}
	
	public static class CalRefleshTimer extends TimerTask{
		public void run(){
			reflectGCalEvent();
		}
	}
	
	
	public static class gCal{
		private static HttpTransport HTTP_TRANSPORT;
		private static final String APP_NAME = "DeadlineGCal";
		private static String jsonPath;
		private static String servAddr;

		private static com.google.api.services.calendar.Calendar calService;
		
		/**
		 * Constructor of GcalService class
		 * @param name
		 * @param pass
		 */
		gCal(String jsonPath,String servAddr){
			this.jsonPath = jsonPath;
			this.servAddr = servAddr;
			
			//get calendar client
			GoogleCredential credit = null;
			credit = getGoogleCredential();
				
			calService = getCalendarClient(credit);
		}
		
		/**
		 * Get O-Authv2 helper for accessing protected resources
		 * @throws IOException 
		 * @throws GeneralSecurityException 
		 */
		public GoogleCredential getGoogleCredential(){
			GoogleCredential credential = null;
			try{
				if(HTTP_TRANSPORT == null) HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				credential = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
	                													.setJsonFactory(new JacksonFactory())
	                													.setServiceAccountId(servAddr)
	                													.setServiceAccountScopes(Arrays.asList(CalendarScopes.CALENDAR))
	                													.setServiceAccountPrivateKeyFromP12File(new File(jsonPath))
	                													.build();
			}catch(Exception exp){
				exp.printStackTrace();
			}
			return credential;
		}
		
		/*Get calendar object*/
		private com.google.api.services.calendar.Calendar getCalendarClient(GoogleCredential credential){
			try{ 
				if(HTTP_TRANSPORT == null) HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			}catch(Exception exc){
				System.out.println(exc.getMessage());
				exc.printStackTrace();
				return null;
			}
			
			return new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT,
					new GsonFactory(),
					credential)
					.setApplicationName(APP_NAME).build();
		}
		
		/**
		 * カレンダーリスト取得
		 */
		public CalendarList getCalendarList() {
			com.google.api.services.calendar.model.CalendarList calList = null;
			try {
				calList = calService.calendarList().list().execute();
				System.out.println(calList.getItems().toString());
				System.out.println("CalList:"+calList.size());
				for(CalendarListEntry item : calList.getItems()){
					System.out.println(item.getSummary());
				}
			}catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return calList;
		}
		
		/**
		 * Get calendar events of specific calendar
		 */
		public List<Event> getEventList(String calId){
			String pageToken = null;
			List<Event> eventList = null;

			/*Get events*/
			try{
				do{
					Events events = calService.events().list(calId).setPageToken(pageToken).execute();//Send request(PROBLEM: RETURNED 404 ERR)
					eventList = events.getItems();
					pageToken = events.getNextPageToken();
				}while(pageToken!=null);
			}catch(Exception exc){
				exc.printStackTrace();
				return null;
			}
			return eventList;
		}
		
		public HashMap<Integer,CalendarEvent> toCalEvents(List<Event> eventList){
			HashMap<Integer,CalendarEvent> result = new HashMap<Integer,CalendarEvent>();

			/*Set events to return value*/
			int id_cnt = 0;
			for(Event evnt : eventList){
				result.put(id_cnt,new CalendarEvent(id_cnt,evnt.getSummary(),new DateTime(evnt.getStart().getDateTime().getValue())));
				id_cnt++;
			}
			return result;
		}
	}
	
	public static class CalendarEvent{
		private int id;//read-only
		public String name;
		public DateTime deadline;
		
		CalendarEvent(int id,String arg_name,DateTime arg_date){
			this.id = id;
			this.name = arg_name;
			this.deadline = arg_date;
		}
		
		int getId(){
			return this.id;
		}
	}
}

