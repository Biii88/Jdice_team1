import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
/**
 * JDice là ứng dụng mô phỏng quá trình tung xúc xắc với giao diện người dùng bằng Swing.
 * Người dùng có thể chọn loại xúc xắc và xem kết quả.
 */
public class JDice {
    static final String CLEAR = "Clear";  // Tên nút xóa kết quả
    static final String ROLL = "Roll Selection";  // Tên nút thực hiện việc tung xúc xắc  
    // Thiết lập logger để ghi lại các sự kiện quan trọng
    private static final Logger logger = Logger.getLogger(JDice.class.getName());   
    /**
     * Hiển thị lỗi và ghi lại lỗi vào log. 
     * @param errorMessage Thông báo lỗi cần hiển thị.
     */
    static void showError(String errorMessage) {
        // Ghi lại lỗi vào log để theo dõi sau
        logger.severe(errorMessage);
    }
    /**
     * Lớp JDiceListener dùng để lắng nghe và xử lý các sự kiện từ các thành phần trong giao diện người dùng.
     */
    private static class JDiceListener implements ActionListener {
        Vector<String> listItems;  // Danh sách các kết quả sẽ hiển thị trong giao diện
        JList<String> resultList;  // JList để hiển thị kết quả
        JComboBox<String> inputBox;  // JComboBox cho phép chọn chuỗi xúc xắc
        long lastEvent;  // Biến dùng để tránh sự kiện kép khi người dùng nhập liệu       
        /**
         * Khởi tạo JDiceListener với các thành phần giao diện.
         * 
         * @param resultList JList dùng để hiển thị kết quả.
         * @param inputBox JComboBox cho phép chọn chuỗi xúc xắc.
         */
        public JDiceListener(JList<String> resultList, JComboBox<String> inputBox) {
            this.listItems = new Vector<>();
            this.resultList = resultList;
            this.inputBox = inputBox;
            this.lastEvent = 0;  // Khởi tạo thời gian sự kiện ban đầu
        }
        /**
         * Xử lý sự kiện khi người dùng thao tác với giao diện.
         * 
         * @param e Đối tượng ActionEvent chứa thông tin về sự kiện.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // Kiểm tra nếu sự kiện đã xảy ra trước đó để tránh sự kiện kép
            if (e.getWhen() == lastEvent)
                return;
            lastEvent = e.getWhen();
            // Nếu sự kiện là từ JComboBox hoặc nút "Roll Selection"
            if (e.getSource() instanceof JComboBox || e.getActionCommand().equals(ROLL)) {
                String selectedItem = inputBox.getSelectedItem().toString();
                String[] arr = selectedItem.split("=");  // Chia chuỗi xúc xắc thành các phần
                String name = "";  // Tên sẽ được thêm vào nếu có
                // Sử dụng StringBuilder để nối chuỗi
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 0; i < arr.length - 2; i++) {
                    nameBuilder.append(arr[i]).append("=");
                }
                if (arr.length >= 2) {
                    nameBuilder.append(arr[arr.length - 2]);
                }
                name = nameBuilder.toString();               
                // Thực hiện việc tung xúc xắc
                doRoll(name, arr[arr.length - 1]);
            } else if (e.getActionCommand().equals(CLEAR)) { 
                // Nếu người dùng chọn nút Clear, xóa kết quả
                doClear();
            } else {
                // Nếu là sự kiện khác, thực hiện việc roll xúc xắc với chuỗi tương ứng
                doRoll(null, e.getActionCommand());
            }
        }
        /**
         * Xóa kết quả trong danh sách và cập nhật lại giao diện.
         */
        private void doClear() {
            listItems.clear();  // Xóa danh sách kết quả
            resultList.setListData(listItems);  // Cập nhật lại JList
            logger.info("Danh sách kết quả đã được xóa.");  // Ghi lại sự kiện xóa
        }
        /**
         * Thực hiện việc tung xúc xắc và hiển thị kết quả.
         * @param name Tên (nếu có) sẽ được hiển thị trước kết quả.
         * @param diceString Chuỗi đại diện cho các xúc xắc cần tung.
         */
        private void doRoll(String name, String diceString) {
            String prepend = "";  // Tiền tố được thêm vào kết quả
            int start = 0;  // Vị trí bắt đầu hiển thị kết quả
            Vector<DieRoll> rolls = DiceParser.parseRoll(diceString);  // Phân tích chuỗi xúc xắc
            // Kiểm tra nếu chuỗi xúc xắc không hợp lệ
            if (rolls == null) {
                showError("Chuỗi xúc xắc không hợp lệ: " + diceString);
                return;
            }
            // Nếu có tên, thêm tên vào kết quả
            if (name != null) {
                listItems.add(0, name);  // Thêm tên vào đầu danh sách
                start = 1;  // Vị trí bắt đầu sẽ là 1 nếu có tên
                prepend = "  ";  // Tiền tố cho các kết quả
            }
            // Thêm kết quả của mỗi lần roll vào danh sách
            int[] selectionIndices = new int[start + rolls.size()];
            for (int i = 0; i < rolls.size(); i++) {
                DieRoll dr = rolls.get(i);
                RollResult rr = dr.makeRoll();  // Lấy kết quả của xúc xắc
                String toAdd = prepend + dr + "  =>  " + rr;  // Tạo chuỗi kết quả
                listItems.add(i + start, toAdd);  // Thêm kết quả vào danh sách
            }
            // Chọn tất cả các kết quả trong JList
            for (int i = 0; i < selectionIndices.length; i++) {
                selectionIndices[i] = i;
            }        
            resultList.setListData(listItems);  // Cập nhật dữ liệu cho JList
            resultList.setSelectedIndices(selectionIndices);  // Chọn tất cả các kết quả         
            // Ghi lại sự kiện "Roll" vào log
            logger.info("Đã thực hiện roll xúc xắc: " + diceString);
        }
    }
    /**
     * Hàm main để khởi động ứng dụng, thiết lập giao diện và các sự kiện.
     * @param args Tham số đầu vào từ dòng lệnh, chứa đường dẫn đến file cấu hình.
     */
    public static void main(String[] args) {
        Vector<String> options = new Vector<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));  // Đọc file cấu hình
            String line;
            while ((line = br.readLine()) != null) {
                options.add(line);  // Thêm từng dòng vào danh sách các tùy chọn
            }
        } catch (IOException ioe) {
            showError("Không thể đọc file đầu vào: " + args[0]);
            ioe.printStackTrace();
        }
        // Tạo cửa sổ ứng dụng và thiết lập giao diện
        JFrame jf = new JFrame("Dice Roller");
        Container c = jf.getContentPane();
        c.setLayout(new BorderLayout());
        JList<String> resultList = new JList<>();  // JList để hiển thị kết quả
        c.add(resultList, BorderLayout.CENTER);
        JComboBox<String> inputBox = new JComboBox<>(options);  // JComboBox để chọn loại xúc xắc
        inputBox.setEditable(true);  // Cho phép người dùng nhập trực tiếp
        c.add(inputBox, BorderLayout.NORTH);
        JDiceListener listener = new JDiceListener(resultList, inputBox);  // Lắng nghe các sự kiện
        inputBox.addActionListener(listener);  // Thêm listener cho JComboBox
        // Tạo các nút bấm trên giao diện
        JPanel rightSidePanel = new JPanel();
        rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.Y_AXIS));
        String[] buttonLabels = {ROLL, "d4", "d6", "d8", "d10", "d12", "d20", "d100", CLEAR};      
        for (String label : buttonLabels) {
            JButton newButton = new JButton(label);  // Tạo nút với nhãn tương ứng
            rightSidePanel.add(newButton);  // Thêm nút vào giao diện
            newButton.addActionListener(listener);  // Thêm listener cho nút
        }      
        c.add(rightSidePanel, BorderLayout.EAST);  // Thêm panel chứa nút vào cửa sổ
        // Thiết lập cửa sổ và hiển thị
        jf.setSize(450, 500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
