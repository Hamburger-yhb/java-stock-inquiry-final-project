/**
 * 股票查询系统 - 关于对话框
 * 
 * 技术支持: Perrin PerrinYong@163.com
 * 欢迎联系洽谈合作
 */
package harry.stockinqbasic.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AboutDialog extends JDialog {
    
    public AboutDialog(JFrame parent) {
        super(parent, "关于股票查询系统", true);
        initializeDialog();
    }
    
    private void initializeDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(450, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // 标题标签
        JLabel titleLabel = new JLabel("股票查询系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 版本标签
        JLabel versionLabel = new JLabel("版本 1.0", JLabel.CENTER);
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 描述标签
        JLabel descLabel = new JLabel("专业的股票行情查询工具", JLabel.CENTER);
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        // 分隔线
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(300, 1));
                
        // 功能特性面板
        JPanel featurePanel = createFeaturePanel();
        
        // 确定按钮
        JButton okButton = new JButton("确定");
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // 添加组件
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(versionLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(descLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(separator);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(featurePanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(okButton);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
   
    
    private JPanel createFeaturePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("主要功能"));
        
        String[] features = {
            "📈 多市场股票查询 (港股/美股/A股)",
            "⚡ 实时报价和涨跌信息",
            "🔧 灵活的API配置管理",
            "💾 本地配置缓存",
            "📊 直观的表格数据展示"
        };
        
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(featureLabel);
            panel.add(Box.createVerticalStrut(3));
        }
        
        return panel;
    }
}