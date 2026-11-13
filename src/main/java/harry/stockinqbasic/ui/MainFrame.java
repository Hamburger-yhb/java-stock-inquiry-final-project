package harry.stockinqbasic.ui;

import harry.stockinqbasic.client.ItickApiClient;
import harry.stockinqbasic.model.StockView;
import harry.stockinqbasic.service.*;
import harry.stockinqbasic.util.SimpleLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 股票查询系统主界面
 * 支持多市场股票实时查询，包括港股、美股、A股等
 *
 */
public class MainFrame extends JFrame {

    private JTextField regionField;
    private JTextField codeField;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton settingsButton;
    private JLabel statusLabel;
    
    // 服务层组件
    private final StockSearchWithQuoteService stockSearchService;
    private final StockService stockService;

    public MainFrame() {
        SimpleLogger.info("初始化主界面...");
        
        // 初始化服务层
        ItickApiClient apiClient = new ItickApiClient();
        StockSearchService searchSvc = new StockSearchService(apiClient);
        StockInfoService infoSvc = new StockInfoService(apiClient);
        StockQuoteService quoteSvc = new StockQuoteService(apiClient);
        this.stockService = new StockService(infoSvc, quoteSvc);
        this.stockSearchService = new StockSearchWithQuoteService(searchSvc, stockService);
        
        SimpleLogger.debug("服务层组件初始化完成");

        initializeComponents();
        setupMenuBar();
        setupLayout();
        setupEventHandlers();
        
        // 设置窗口属性
        setTitle("股票查询系统 v1.0 ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null); // 居中显示
        
        // 显示欢迎信息
        statusLabel.setText("欢迎使用股票查询系统！请输入区域和代码前缀进行搜索。");
        
        SimpleLogger.info("主界面初始化完成");
    }

    /**
     * 初始化UI组件
     */
    private void initializeComponents() {
        // 输入字段
        regionField = new JTextField("HK", 5);
        codeField = new JTextField(10);
        
        // 按钮
        searchButton = new JButton("搜索股票");
        refreshButton = new JButton("刷新数据");
        settingsButton = new JButton("设置");
        
        // 状态标签
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLUE);
        
        // 表格
        String[] columnNames = {"代码", "名称", "昨收价", "当前价", "涨跌额", "涨跌幅(%)", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不可编辑
            }
        };
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setRowHeight(25);
        
        // 设置列宽
        stockTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 代码
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(180); // 名称
        stockTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 昨收价
        stockTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 当前价
        stockTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 涨跌额
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 涨跌幅
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(50);  // 状态
        
        // 设置颜色渲染
        stockTable.setDefaultRenderer(Object.class, new StockTableCellRenderer());
    }

    /**
     * 设置菜单栏
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 设置菜单
        JMenu settingsMenu = new JMenu("设置");
        JMenuItem tokenConfigItem = new JMenuItem("配置Token");
        tokenConfigItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.openSettings();
            }
        });
        settingsMenu.add(tokenConfigItem);
        menuBar.add(settingsMenu);
        

        
        setJMenuBar(menuBar);
    }

    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("股票搜索"));
        searchPanel.add(new JLabel("区域:"));
        searchPanel.add(regionField);
        searchPanel.add(new JLabel("代码前缀:"));
        searchPanel.add(codeField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(settingsButton);
        
        // 中央表格面板
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("股票列表"));
        
        // 底部状态面板
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * 设置事件处理
     */
    private void setupEventHandlers() {
        // 搜索按钮事件
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // 刷新按钮事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentData();
            }
        });
        
        // 设置按钮事件
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSettings();
            }
        });
        
        // 回车键搜索
        codeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
    }

    /**
     * 执行股票搜索
     */
    private void performSearch() {
        String region = regionField.getText().trim().toUpperCase();
        String codePrefix = codeField.getText().trim();
        
        if (region.isEmpty() || codePrefix.isEmpty()) {
            SimpleLogger.error("用户输入无效: 区域=" + region + ", 代码前缀=" + codePrefix);
            showError("请输入区域和代码前缀！");
            return;
        }
        
        // 在后台线程执行搜索，避免阻塞UI
        searchButton.setEnabled(false);
        statusLabel.setText("正在搜索 " + region + ":" + codePrefix + "...");
        
        SimpleLogger.info("开始搜索股票: 区域=" + region + ", 代码前缀=" + codePrefix);
        
        SwingWorker<List<StockView>, Void> worker = new SwingWorker<List<StockView>, Void>() {
            @Override
            protected List<StockView> doInBackground() throws Exception {
                return stockSearchService.searchAndQuote(region, codePrefix);
            }
            
            @Override
            protected void done() {
                try {
                    List<StockView> results = get();
                    updateTable(results);
                    SimpleLogger.info("搜索成功: 区域=" + region + ", 代码前缀=" + codePrefix + ", 结果数=" + results.size());
                    statusLabel.setText("搜索完成，找到 " + results.size() + " 支股票。");
                } catch (Exception ex) {
                    SimpleLogger.error("搜索失败: 区域=" + region + ", 代码前缀=" + codePrefix + ", 错误=" + ex.getMessage());
                    showError("搜索失败: " + ex.getMessage());
                } finally {
                    searchButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    /**
     * 刷新当前数据
     */
    private void refreshCurrentData() {
        if (tableModel.getRowCount() == 0) {
            SimpleLogger.info("用户尝试刷新但没有数据");
            showError("没有数据可刷新，请先搜索股票！");
            return;
        }
        
        refreshButton.setEnabled(false);
        statusLabel.setText("正在刷新数据...");
        
        SimpleLogger.info("开始刷新数据, 总数=" + tableModel.getRowCount());
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String fullCode = (String) tableModel.getValueAt(i, 0);
                    String[] parts = fullCode.split(":");
                    if (parts.length == 2) {
                        String region = parts[0];
                        String code = parts[1];
                        
                        try {
                            final StockView view = stockService.getStockView(region, code);
                            final int rowIndex = i;
                            // 在EDT线程更新UI
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateTableRow(rowIndex, view);
                                }
                            });
                        } catch (Exception ex) {
                            SimpleLogger.error("刷新失败: 股票=" + fullCode + ", 错误=" + ex.getMessage());
                        }
                    }
                }
                return null;
            }
            
            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                statusLabel.setText("数据刷新完成。");
                SimpleLogger.info("数据刷新完成");
            }
        };
        worker.execute();
    }

    /**
     * 更新表格数据
     */
    private void updateTable(List<StockView> stockViews) {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        // 添加新数据
        for (StockView view : stockViews) {
            Object[] rowData = {
                view.getRegion() + ":" + view.getCode(),
                view.getName(),
                String.format("%.2f", view.getPrevClose()),
                String.format("%.2f", view.getLastPrice()),
                String.format("%.2f", view.getChange()),
                String.format("%.2f", view.getChangePct()),
                getStatusText(view)
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * 更新表格中的单行数据
     */
    private void updateTableRow(int row, StockView view) {
        tableModel.setValueAt(String.format("%.2f", view.getPrevClose()), row, 2);
        tableModel.setValueAt(String.format("%.2f", view.getLastPrice()), row, 3);
        tableModel.setValueAt(String.format("%.2f", view.getChange()), row, 4);
        tableModel.setValueAt(String.format("%.2f", view.getChangePct()), row, 5);
        tableModel.setValueAt(getStatusText(view), row, 6);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(StockView view) {
        if (view.isUp()) {
            return "↗";
        } else if (view.isDown()) {
            return "↘";
        } else {
            return "→";
        }
    }

    /**
     * 显示错误信息
     */
    private void showError(String message) {
        statusLabel.setText("错误: " + message);
        statusLabel.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
        statusLabel.setForeground(Color.BLUE);
    }
    
    /**
     * 打开设置对话框
     */
    private void openSettings() {
        SimpleLogger.info("用户打开设置对话框");
        
        TokenConfigDialog dialog = new TokenConfigDialog(this);
        dialog.setVisible(true);
        
        // 检查配置是否更改
        if (dialog.isConfigChanged()) {
            SimpleLogger.info("Token配置已更改，建议重新搜索以验证配置");
            statusLabel.setText("配置已更新，建议重新搜索以验证新配置。");
            statusLabel.setForeground(Color.ORANGE);
        }
    }

    /**
     * 表格单元格渲染器，用于设置颜色
     */
    private static class StockTableCellRenderer extends JLabel implements TableCellRenderer {
        
        public StockTableCellRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setText(value != null ? value.toString() : "");
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                
                // 根据涨跌情况设置颜色
                if (column == 4 || column == 5) { // 涨跌额和涨跌幅列
                    String text = value != null ? value.toString() : "";
                    try {
                        double val = Double.parseDouble(text);
                        if (val > 0) {
                            setForeground(Color.RED); // 上涨用红色
                        } else if (val < 0) {
                            setForeground(new Color(0, 128, 0)); // 下跌用绿色
                        } else {
                            setForeground(Color.BLACK); // 平盘用黑色
                        }
                    } catch (NumberFormatException e) {
                        setForeground(Color.BLACK);
                    }
                } else if (column == 6) { // 状态列
                    String text = value != null ? value.toString() : "";
                    if ("↗".equals(text)) {
                        setForeground(Color.RED);
                    } else if ("↘".equals(text)) {
                        setForeground(new Color(0, 128, 0));
                    } else {
                        setForeground(Color.BLACK);
                    }
                } else {
                    setForeground(table.getForeground());
                }
            }
            
            return this;
        }
    }
}