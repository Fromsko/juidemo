import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatWindow {
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextArea inputField;
    private JButton sendButton;
    private SimpleDateFormat dateFormat;
    private ImageIcon userIcon;
    private ImageIcon replyIcon;
    private JFrame frame = new JFrame("QQ聊天");
    private Point point = centerShow();
    private String baseAPI = "https://api.aigcbest.top/v1/chat/completions";
    private String openKEY = "";

    public ChatWindow() {
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        inputField = new JTextArea();
        inputField.setRows(3);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    sendButton.doClick();
                }
            }
        });
        bottomPanel.add(new JScrollPane(inputField), BorderLayout.CENTER);

        sendButton = new JButton("发送");
        sendButton.setBackground(Color.WHITE);
        sendButton.setForeground(Color.GREEN);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                sendMessage("Me", text, true, userIcon);
                inputField.setText("");
                sendMessage("Bot", chatGPT(text), false, replyIcon);
            }
        });
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        dateFormat = new SimpleDateFormat("HH:mm:ss");

        // 假设用户和回复者的头像分别存储在"user.png"和"bot.png"文件中
        Image userImage = new ImageIcon("src/res/user.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        userIcon = new ImageIcon(userImage);
        Image replyImage = new ImageIcon("src/res/bot.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        replyIcon = new ImageIcon(replyImage);

        // 设置显示位置
        frame.setLocation(point);
        frame.setVisible(true);
        pop(null);
    }

    private Point centerShow() {
        // 获取屏幕大小
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // 计算位置
        int width = (int) ((screenSize.getWidth() - frame.getWidth()) / 2);
        int height = (int) ((screenSize.getHeight() - frame.getHeight()) / 2);

        // 返回一个包含两个坐标的Point对象
        return new Point(width, height);
    }

    private String chatGPT(String msg) {
        try {
            URL url = new URL(baseAPI);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + openKEY);
            conn.setDoOutput(true);

            JsonObject message = Json.createObjectBuilder()
                    .add("role", "user")
                    .add("content", msg)
                    .build();

            JsonArray messages = Json.createArrayBuilder()
                    .add(message)
                    .build();

            JsonObject root = Json.createObjectBuilder()
                    .add("model", "gpt-3.5-turbo")
                    .add("messages", messages)
                    .add("temperature", 0.7)
                    .build();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = root.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JsonObject jsonResponse = Json.createReader(new StringReader(response.toString())).readObject();
                return jsonResponse.getJsonArray("choices").getJsonObject(0).getString("message");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private void sendMessage(String nickname, String message, boolean isSelf, ImageIcon icon) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));

        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getCircleImage(icon.getImage())));
        iconPanel.add(iconLabel);
        JLabel nicknameLabel = new JLabel(nickname);
        iconPanel.add(nicknameLabel);
        messagePanel.add(iconPanel);

        // 头像右击
        iconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) pop(e);
            }
        });


        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setMaximumSize(new Dimension(frame.getWidth() - 100, Integer.MAX_VALUE));

        JTextArea messageArea = new JTextArea(0, 20);
        messageArea.setText(message);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        textPanel.add(scrollPane, BorderLayout.CENTER);
        // 双击事件
        messageArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 检测双击
                    StringSelection stringSelection = new StringSelection(messageArea.getText());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        });


        JLabel timeLabel = new JLabel(dateFormat.format(new Date()));
        textPanel.add(timeLabel, BorderLayout.PAGE_END);

        if (isSelf) {
            messagePanel.add(Box.createHorizontalStrut(10));
            messagePanel.add(textPanel);
            messagePanel.add(Box.createHorizontalStrut(10));
            messagePanel.add(iconPanel);
        } else {
            messagePanel.add(iconPanel);
            messagePanel.add(Box.createHorizontalStrut(10));
            messagePanel.add(textPanel);
            messagePanel.add(Box.createHorizontalStrut(10));
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        // 在消息发送后
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JScrollBar vertical = ChatWindow.this.scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private void pop(MouseEvent e) {
        { // 检测右击
            JDialog dialog = new JDialog(frame, "API and KEY", true);
            dialog.setSize(200, 200);
            dialog.setLayout(new GridLayout(3, 2));

            JLabel apiLabel = new JLabel("OpenAI 代理地址:");
            JTextField apiField = new JTextField(baseAPI); // 显示之前填写的API
            apiField.setPreferredSize(new Dimension(120, 20));
            JLabel keyLabel = new JLabel("OpenAI 密钥:");
            JTextField keyField = new JPasswordField(openKEY); // 显示之前填写的KEY
            keyField.setPreferredSize(new Dimension(120, 20));

            JButton submitButton = new JButton("提交");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // 在点击提交按钮后获取并存储API和KEY
                    baseAPI = apiField.getText();
                    openKEY = keyField.getText();
                    dialog.dispose();
                }
            });

            dialog.add(apiLabel);
            dialog.add(apiField);
            dialog.add(keyLabel);
            dialog.add(keyField);
            dialog.add(submitButton);

            dialog.pack();
            if (e != null) {
                dialog.setLocation(e.getLocationOnScreen()); // 在右击的位置弹出对话框
            }
            dialog.setVisible(true);
        }
    }

    private Image getCircleImage(Image img) {
        BufferedImage circleBuffer = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, img.getWidth(null), img.getHeight(null)));
        g2.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null), null);
        return circleBuffer;
    }

    public static void main(String[] args) {
        new ChatWindow();
    }
}

/*
 * 出现了如下问题：
 *   1.聊天信息到最下面的时候会被遮挡，看不到而且不能滑动。
 *   2.聊天界面的聊天输入框换行后尺寸不会变化，导致看不清其余的输入。
 *   3.左右布局似乎不对，左布局的实现正常了，但是右布局显示不正常了。右边布局应该是己方信息框在头像和昵称左侧且紧挨着。
 *   4.当使用输入法的时候按下了第一次回车也会导致信息直接发送，因此应该要对回车键进行一个二次判断，防止误触。
 *   5.当长按信息框的时候，希望能够复制该信息。
 *   6.信息框应该和信息的长度和大小高度匹配，而不是无论发多长信息都是一样的大小尺寸。
 *   7.信息框和聊天输入框以及发送按钮需要变得好看一些，允许对原有组件进行重写来达到好看的样式。
 * 已经解决了
 * */