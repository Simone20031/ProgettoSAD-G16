package com.musicplayer.command;

import java.util.Stack;

public class UndoManager {
    private final Stack<Command> undoStack;

    public UndoManager() {
        this.undoStack = new Stack<>();
    }

    public void aggiungiComando(Command cmd) {
        undoStack.push(cmd);
    }

    public boolean annullaUltimaOperazione() throws Exception {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            cmd.annulla();
            return true;
        }
        return false;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void svuota() {
        undoStack.clear();
    }
}
