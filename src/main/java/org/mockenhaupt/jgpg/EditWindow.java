package org.mockenhaupt.jgpg;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;

public class EditWindow implements JGPGProcess.EncrypionListener
{
    private JDialog editWindow;
    private JEditorPane editorPane;
    private JTextArea textAreaStatus;
    private JComboBox<String> comboBoxDirectories;
    private JTextField textFieldFilename;
    final private JGPGProcess jgpgProcess;
    private boolean modified = false;
    private JButton cancelButton;
    private JButton saveButton;
    final private JFrame parentWindow;


    final private List<String> directories = new ArrayList<>();

    public EditWindow (JFrame parent, JGPGProcess jgpgProcess)
    {
        this.jgpgProcess = jgpgProcess;
        this.parentWindow = parent;
        init(parent);
        jgpgProcess.addEncryptionListener(this);
    }


    public void setDirectories (List<String> directories)
    {
        this.directories.clear();
        this.directories.addAll(directories);

        comboBoxDirectories.setModel(new DefaultComboBoxModel<String>()
        {
            @Override
            public int getSize ()
            {
                return directories.size();
            }

            @Override
            public String getElementAt (int index)
            {
                return directories.get(index);
            }
        });
        if (comboBoxDirectories.getModel().getSize() > 0) comboBoxDirectories.setSelectedIndex(0);
    }

    public List<String> getDirectories ()
    {
        return directories;
    }

    public void setText (String text, String status, String filename)
    {
        if (editorPane == null)
        {
            return;
        }

        editorPane.setText(text);
        setModified(false);
        textFieldFilename.setText(filename);
        textAreaStatus.setText(status);
    }

    public boolean isModified ()
    {
        return modified;
    }

    public void setModified (boolean modified)
    {
        this.saveButton.setEnabled(modified);
        this.modified = modified;
    }

    private void init (JFrame parent)
    {
        if (editWindow == null)
        {
            editWindow = new JDialog(parent, "JGPG Edit", true);
            editWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            URL url = this.getClass().getResource("kgpg_identity.png");
            editWindow.setIconImage(Toolkit.getDefaultToolkit().createImage(url));

            editorPane = new JEditorPane();
            editorPane.setPreferredSize(new Dimension(600, 600));
            editorPane.getDocument()
                    .addDocumentListener(new DocumentListener()
                    {
                        @Override
                        public void insertUpdate (DocumentEvent documentEvent)
                        {
                            setModified(true);
                        }

                        @Override
                        public void removeUpdate (DocumentEvent documentEvent)
                        {
                            setModified(true);
                        }

                        @Override
                        public void changedUpdate (DocumentEvent documentEvent)
                        {
                            setModified(true);
                        }
                    });

            editWindow.setLayout(new BorderLayout());
            editWindow.add(editorPane, BorderLayout.CENTER);
            editWindow.add(commandToolbar(), BorderLayout.NORTH);

            textAreaStatus = new JTextArea();
            textAreaStatus.setEditable(false);
            editWindow.add(textAreaStatus, BorderLayout.SOUTH);

            editWindow.pack();
            editWindow.setVisible(false);
            setModified(false);
            setDirectories(jgpgProcess.getSecretdirs());
        }
    }


    public void show ()
    {
        editWindow.setVisible(true);
    }


    private JToolBar commandToolbar ()
    {
        JToolBar jToolBar = new JToolBar();

        // Button: Cancel
        cancelButton = new JButton("Cancel / Close");
        cancelButton.addActionListener(actionEvent ->
        {
            if (!modified || OK_OPTION == JOptionPane.showConfirmDialog(parentWindow,
                    "File is modified, close discarding changes?",
                    "JGPG Close Confirmation", OK_CANCEL_OPTION))
            {
                editWindow.setVisible(false);
                editWindow.dispose();
            }

        });


        // Button: Save
        saveButton = new JButton("Save / Encrypt");
        saveButton.addActionListener(actionEvent ->
        {
            jgpgProcess.encrypt(textFieldFilename.getText(), editorPane.getText(), "4ADE6739", EditWindow.this);
        });


        textFieldFilename = new JTextField();
        textFieldFilename.setEnabled(false);

        comboBoxDirectories = new JComboBox<>();
        comboBoxDirectories.setVisible(false);

        jToolBar.add(saveButton);
        jToolBar.add(cancelButton);
        jToolBar.add(comboBoxDirectories);
        jToolBar.add(textFieldFilename);

        return jToolBar;
    }

    @Override
    public void handleGpgEncryptResult (String out, String err, String filename, Object clientData)
    {
        SwingUtilities.invokeLater(() -> {
            if (err == null || err.isEmpty()) {
                textAreaStatus.setText("Successfully encrypted " + filename);
                setModified(false);
            }
        });
    }
}
