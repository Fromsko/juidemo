import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class QQLogin {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public QQLogin() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("QQ登录");
        frame.setBounds(100, 100, 300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建自定义面板作为内容面板
        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景图
                ImageIcon imageIcon = new ImageIcon("src/res/background.png"); // 替换为你的背景图路径
                Image image = imageIcon.getImage();
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        };
        frame.setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblUsername = new JLabel("用户名：");
        lblUsername.setBounds(50, 30, 80, 20);
        contentPane.add(lblUsername);

        usernameField = new JTextField();
        usernameField.setBounds(100, 30, 120, 20);
        contentPane.add(usernameField);
        usernameField.setColumns(10);

        JLabel lblPassword = new JLabel("密码：");
        lblPassword.setBounds(50, 70, 80, 20);
        contentPane.add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 70, 120, 20);
        contentPane.add(passwordField);

        JButton btnLogin = getjButton();
        contentPane.add(btnLogin);

        frame.setVisible(true);
    }

    private JButton getjButton() {
        JButton btnLogin = new JButton("登录");
        btnLogin.setBounds(100, 120, 100, 30);

        // 添加动作监听器
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean loginStatus = validateLogin(usernameField, passwordField);
                if (loginStatus) {
                    // 在这里可以添加登录逻辑处理，例如验证用户名密码等
                    JOptionPane.showMessageDialog(frame, "登录成功！欢迎使用QQ。");
                    frame.dispose(); // 关闭当前页面的窗口
                    new ChatWindow(); // 创建新的HomePage对象
                } else {
                    JOptionPane.showMessageDialog(frame, "登录失败!");
                }
            }
        });
        return btnLogin;
    }

    private boolean validateLogin(JTextField usernameField, JPasswordField passwordField) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        return username.equals("root") && password.equals("root");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new QQLogin();
            }
        });
    }
}
