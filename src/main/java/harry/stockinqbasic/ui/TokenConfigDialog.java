package harry.stockinqbasic.ui;

import harry.stockinqbasic.client.ItickApiClient;
import harry.stockinqbasic.util.SimpleLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Token配置对话框
 * 支持iTick API Token的设置、测试和管理
 *
 */
public class TokenConfigDialog extends JDialog {
    
    private JTextField tokenField;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton testButton;
    private JLabel statusLabel;
    private boolean configChanged = false;
    
    public TokenConfigDialog(Frame parent) {
        super(parent, "iTick API Token 配置", true);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadCurrentToken();
        
        // 设置对话框属性
        setSize(500, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        tokenField = new JTextField(50);
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        testButton = new JButton("测试连接");
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部说明
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("请输入您的 iTick API Token:");
        // contactLabel.setFont(contactLabel.getFont().deriveFont(11f));
        // contactLabel.setForeground(Color.GRAY);
        
        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(5));
        // topPanel.add(contactLabel);
        
        // 中央输入区域
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(new JLabel("Token:"), BorderLayout.WEST);
        centerPanel.add(tokenField, BorderLayout.CENTER);
        centerPanel.add(testButton, BorderLayout.EAST);
        
        // 底部按钮和状态
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理
     */
    private void setupEventHandlers() {
        // 保存按钮
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToken();
            }
        });
        
        // 取消按钮
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 测试连接按钮
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testConnection();
            }
        });
        
        // 回车保存
        tokenField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToken();
            }
        });
    }
    
    /**
     * 加载当前Token
     */
    private void loadCurrentToken() {
        String currentToken = ItickApiClient.getCurrentToken();
        tokenField.setText(currentToken);
        statusLabel.setText("当前Token: " + maskToken(currentToken));
    }
    
    /**
     * 保存Token
     */
    private void saveToken() {
        String token = tokenField.getText().trim();
        
        if (token.isEmpty()) {
            showMessage("Token不能为空！", Color.RED);
            return;
        }
        
        if (token.length() < 16) {
            showMessage("Token长度太短，请检查是否完整！", Color.RED);
            return;
        }
        
        try {
            // 设置新Token
            ItickApiClient.setToken(token);
            configChanged = true;
            showMessage("Token保存成功！", Color.GREEN);
            
            // 延迟关闭对话框
            Timer timer = new Timer(1500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();
            
        } catch (Exception e) {
            SimpleLogger.error("保存Token失败: " + e.getMessage());
            showMessage("保存失败: " + e.getMessage(), Color.RED);
        }
    }
    
    /**
     * 测试连接
     */
    private void testConnection() {
        String token = tokenField.getText().trim();
        
        if (token.isEmpty()) {
            showMessage("请先输入Token！", Color.RED);
            return;
        }
        
        showMessage("正在测试连接...", Color.BLUE);
        testButton.setEnabled(false);
        
        // 在后台线程测试连接
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ItickApiClient.testToken(token);
            }
            
            @Override
            protected void done() {
                testButton.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        showMessage("连接测试成功！Token有效。", Color.GREEN);
                    } else {
                        showMessage("连接测试失败！请检查Token是否正确。", Color.RED);
                    }
                } catch (Exception e) {
                    showMessage("测试过程中出现错误: " + e.getMessage(), Color.RED);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * 显示消息
     */
    private void showMessage(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    /**
     * 隐藏Token显示
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 8) + "***" + token.substring(token.length() - 4);
    }
    
    /**
     * 检查配置是否已更改
     */
    public boolean isConfigChanged() {
        return configChanged;
    }
}