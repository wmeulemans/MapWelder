package nl.tue.mapwelder.io;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import nl.tue.geometrycore.gui.sidepanel.ComboTabItem;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.mapwelder.data.PlaneMap;
import nl.tue.mapwelder.gui.Data;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class Format implements ComboTabItem {

    protected static final JFileChooser choose = new JFileChooser(".");

    final protected Data data;
    final String name;
    final FileNameExtensionFilter filter;

    public Format(Data data, String name, String... extensions) {
        this.data = data;
        this.name = name;
        this.filter = new FileNameExtensionFilter(name, extensions);
    }

    public void load() {
        try {
            choose.resetChoosableFileFilters();
            choose.addChoosableFileFilter(filter);
            choose.setFileFilter(filter);
            int result = choose.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                data.map = load(choose.getSelectedFile());
                data.mapChanged(true);
                data.draw.zoomToFit();
            }
        } catch (IOException ex) {
            Logger.getLogger(Format.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void save() {
        choose.resetChoosableFileFilters();
        choose.addChoosableFileFilter(filter);
        choose.setFileFilter(filter);
        int result = choose.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                save(choose.getSelectedFile(), data.map);
            } catch (IOException ex) {
                Logger.getLogger(Format.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void createGUI(SideTab tab) {
        if (canLoad()) {
            tab.addButton("Open file", (e) -> load());
        }

        if (canSave()) {
            tab.addButton("Save file", (e) -> save());
        }
    }

    @Override
    public String toString() {
        return name;
    }

    protected abstract PlaneMap load(File file) throws IOException;

    protected abstract void save(File file, PlaneMap map) throws IOException;

    protected abstract boolean canLoad();

    protected abstract boolean canSave();

}
