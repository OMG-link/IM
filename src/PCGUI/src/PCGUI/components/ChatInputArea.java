package PCGUI.components;

import IM.Client;
import PCGUI.RoomFrame;

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
        new DropTarget(this,DnDConstants.ACTION_COPY_OR_MOVE,this);
    }

    private boolean uploadTransferable(Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                @SuppressWarnings("unchecked")
                java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : fileList) {
                    try {
                        handler.uploadFile(file);
                    } catch (FileNotFoundException ignored) {
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
        }else{
            dtde.rejectDrop();
        }
    }
}
