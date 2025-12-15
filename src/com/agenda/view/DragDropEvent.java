package com.agenda.view;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

public class DragDropEvent {

    public interface DropCallback {
        void onDrop(String data);
    }

    // Méthode simplifiée pour activer le drag
    public static void enableDrag(JComponent component, final String data) {
        component.setTransferHandler(new TransferHandler("text") {
            @Override
            protected Transferable createTransferable(JComponent c) {
                return new StringSelection(data);
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        });

        // MouseListener pour détecter le début du drag
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                TransferHandler handler = comp.getTransferHandler();
                handler.exportAsDrag(comp, e, TransferHandler.COPY);
            }
        });
    }

    // Méthode simplifiée pour activer le drop
    public static void enableDrop(JComponent component, final DropCallback callback) {
        component.setTransferHandler(new TransferHandler("text") {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    String droppedData = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    callback.onDrop(droppedData);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        // Feedback visuel simple
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                component.setBackground(new Color(200, 230, 255));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                component.setBackground(Color.WHITE);
            }
        });
    }
}