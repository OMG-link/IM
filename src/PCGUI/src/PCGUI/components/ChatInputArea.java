package PCGUI.components;

import GUI.IConfirmDialogCallback;
import GUI.IFileTransferringPanel;
import IM.Client;
import PCGUI.RoomFrame;
import mutils.ImageUtils;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.ImageType;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChatInputArea extends InputArea implements DropTargetListener {

    private final Client handler;

    private boolean isPasteSuppressed = false;

    public ChatInputArea(RoomFrame roomFrame) {
        super(roomFrame);
        handler = roomFrame.getHandler();
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    public void uploadFile(File file) {
        IFileTransferringPanel panel = handler.getRoomFrame().addFileTransferringPanel(
                file::getName,
                file.length()
        );
        try{
            handler.uploadFile(file, FileTransferType.ChatFile,panel);
        }catch (FileNotFoundException e){
            handler.showInfo(String.format("File %s not found.",file.getAbsolutePath()));
        }
    }

    private boolean uploadTransferable(Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                @SuppressWarnings("unchecked")
                java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : fileList) {
                    if (ImageUtils.isImageFile(file)) {
                        isPasteSuppressed = false; //The checkbox will block the key up message.
                        handler.showCheckBox(
                                "Would you like to send this file as an image?",
                                new IConfirmDialogCallback() {
                                    @Override
                                    public void onPositiveInput() {
                                        try {
                                            handler.sendChatImage(file, ImageType.PNG);
                                        } catch (FileNotFoundException e) {
                                            handler.showInfo("File not found.");
                                        }
                                    }

                                    @Override
                                    public void onNegativeInput() {
                                        uploadFile(file);
                                    }
                                }
                        );
                    } else {
                        uploadFile(file);
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void processKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_V) {
            if (event.getID() == KeyEvent.KEY_PRESSED) {
                if (event.isControlDown()) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable transferable = clipboard.getContents(null);
                    if (!isPasteSuppressed) {
                        isPasteSuppressed = true;
                        if (uploadTransferable(transferable)) return;
                    }
                }
            }
            if (event.getID() == KeyEvent.KEY_RELEASED) {
                isPasteSuppressed = false;
            }
        }

        super.processKeyEvent(event);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            uploadTransferable(dtde.getTransferable());
        } else {
            dtde.rejectDrop();
        }
    }
}
