package org.mockenhaupt.jgpg;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebugWindow
{
    public static int CAT_FILTER = 1;
    public static int CAT_GPG = 1<<1;
    public static int CAT_FAVORITES = 1<<2;
    public static int CAT_LIST = 1<<3;

    enum Category {
        FILTER(CAT_FILTER),
        GPG(CAT_GPG),
        FAV(CAT_FAVORITES),
        LIST(CAT_LIST)
        ;

        private final int value;
        Category (int val) {
            this.value = val;
        }

        public int getValue ()
        {
            return value;
        }
    }

    class CategoryCheckbox extends JCheckBox {
        protected Category category;
        public CategoryCheckbox (Category category)
        {
            super(category.name());
            this.category = category;
            this.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    CategoryCheckbox me = CategoryCheckbox.this;
                    me.handleSelection();
                }
            });
        }

        private void handleSelection ()
        {
            if (isSelected())
            {
                DebugWindow.get().enableDebugCategory(category.getValue());
                debug("DBG: Enabled category " + category);
            }
            else
            {
                DebugWindow.get().disableDebugCategory(category.getValue());
                debug("DBG: Disabled category " + category);
            }
        }
    }

    private int debugMask = 0;

    private static DebugWindow instance;

    private JTextArea debugTextArea;
    private JFrame debugFrame;

    public void enableDebugCategory (int cat)
    {
        debugMask |= cat;
    }
    public void disableDebugCategory (int cat)
    {
        debugMask &= ~cat;
    }

    public boolean isEnabled (int category)
    {
        return (debugMask & category) != 0;
    }

    public int getDebugMask ()
    {
        return debugMask;
    }

    public void setDebugMask (int debugMask)
    {
        this.debugMask = debugMask;
    }

    public static DebugWindow get ()
    {
        if (instance == null)
        {
            instance = new DebugWindow();
        }
        return instance;
    }


    private void initDebugWindow ()
    {
        if (debugFrame == null)
        {
            debugFrame = new JFrame("JGPG Debug");
            debugTextArea = new JTextArea();
            debugFrame.setLayout(new BorderLayout());
            debugTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            JScrollPane debugScrollPane = new JScrollPane(debugTextArea);
            debugScrollPane.setPreferredSize(new Dimension(600, 600));
            debugFrame.getContentPane().add(debugScrollPane, BorderLayout.CENTER);
            JToolBar debugToolbar;
            debugFrame.getContentPane().add(debugToolbar = new JToolBar(), BorderLayout.NORTH);
            JButton debugClear = new JButton("Clear");
            debugClear.addActionListener(e -> debugTextArea.setText(""));
            debugToolbar.add(debugClear);

            JPanel categoryPanel = new JPanel(new FlowLayout());

            for (Category cat : Category.values() )
            {
                CategoryCheckbox categoryCheckbox;
                categoryPanel.add(categoryCheckbox = new CategoryCheckbox(cat));
                categoryCheckbox.setSelected(isEnabled(cat.getValue()));
            }
            debugToolbar.add(categoryPanel);

            debugFrame.pack();
        }
    }

    public void setWindowVisible (boolean visibility)
    {
        if (visibility)
        {
            initDebugWindow();
        }
        if (debugFrame != null)
        {
            debugFrame.setVisible(visibility);
        }
    }

    long debugCount = 0;

    public boolean isActive ()
    {
        return debugFrame != null;
    }


    public void debug (Category cat, String text)
    {
        if (!isActive()) return;
        if (!isEnabled(cat.getValue())) return;

        text = String.format("%-6s %s", cat.name(), text);
        debug(text);
    }


    private void debug (String text)
    {
        if (!isActive()) return;

        debugTextArea.append(String.format("%-9d", debugCount++));
        debugTextArea.append(text);
        debugTextArea.append("\n");
    }


}