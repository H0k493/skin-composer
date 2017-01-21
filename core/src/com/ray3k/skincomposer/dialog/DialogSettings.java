/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2017 Raymond Buckley
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.ray3k.skincomposer.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.ray3k.skincomposer.Main;
import com.ray3k.skincomposer.Spinner;
import com.ray3k.skincomposer.Spinner.Orientation;
import com.ray3k.skincomposer.Spinner.SpinnerStyle;
import com.ray3k.skincomposer.utils.Utils;

public class DialogSettings extends Dialog {
    private final SpinnerStyle spinnerStyle;
    private Integer textureWidth;
    private Integer textureHeight;
    private Integer maxUndos;
    private boolean useStripWhitespace;
    private final Main main;

    public DialogSettings(String title, String windowStyleName, Main main) {
        super(title, main.getSkin(), windowStyleName);
        this.main = main;
        spinnerStyle = new Spinner.SpinnerStyle(main.getSkin().get("spinner-minus-h", Button.ButtonStyle.class), main.getSkin().get("spinner-plus-h", Button.ButtonStyle.class), main.getSkin().get("default", TextField.TextFieldStyle.class));
        
        textureWidth = main.getProjectData().getMaxTextureWidth();
        textureHeight = main.getProjectData().getMaxTextureHeight();
        maxUndos = main.getProjectData().getMaxUndos();
        useStripWhitespace = main.getProjectData().getStripWhitespace();
        setFillParent(true);
        
        populate();
    }

    @Override
    protected void result(Object object) {
        super.result(object);
        
        if ((boolean) object) {
            main.getProjectData().setChangesSaved(false);
            main.getProjectData().setMaxTextureDimensions(MathUtils.nextPowerOfTwo(textureWidth), MathUtils.nextPowerOfTwo(textureHeight));
            main.getProjectData().setMaxUndos(maxUndos);
            main.getProjectData().setStripWhitespace(useStripWhitespace);
            main.getUndoableManager().clearUndoables();
        }
    }

    @Override
    public boolean remove() {
        main.getRootTable().setStatusBarMessage("Settings Updated");
        return super.remove();
    }

    public void populate() {
        Table t = getContentTable();
        
        getButtonTable().padBottom(15.0f);
        
        Label label = new Label("Settings", main.getSkin(), "title");
        t.add(label).colspan(2);
        
        t.row();
        TextButton textButton = new TextButton("Open temp/log directory", main.getSkin());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                try {
                    Utils.openFileExplorer(Gdx.files.local("temp/"));
                } catch (Exception e) {
                    Gdx.app.error(getClass().getName(), "Error opening temp folder", e);
                    main.getDialogFactory().showDialogError("Folder Error...", "Error opening temp folder.\n\nOpen log?");
                }
            }
        });
        textButton.addListener(main.getHandListener());
        t.add(textButton).colspan(2).padTop(15.0f);
        
        t.row();
        textButton = new TextButton("Open preferences directory", main.getSkin());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                try {
                    Utils.openFileExplorer(Gdx.files.external(".prefs/"));
                } catch (Exception e) {
                    Gdx.app.error(getClass().getName(), "Error opening preferences folder", e);
                    main.getDialogFactory().showDialogError("Folder Error...", "Error opening preferences folder.\n\nOpen log?");
                }
            }
        });
        textButton.addListener(main.getHandListener());
        t.add(textButton).colspan(2);
        
        if (main.getProjectData().areChangesSaved() && main.getProjectData().getSaveFile().exists()) {
            t.row();
            textButton = new TextButton("Open project/import directory", main.getSkin());
            textButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    try {
                        Utils.openFileExplorer(main.getProjectData().getSaveFile().sibling(main.getProjectData().getSaveFile().nameWithoutExtension() + "_data"));
                    } catch (Exception e) {
                        Gdx.app.error(getClass().getName(), "Error opening project folder", e);
                        main.getDialogFactory().showDialogError("Folder Error...", "Error opening project folder\n\nOpen log?");
                    }
                }
            });
            textButton.addListener(main.getHandListener());
            t.add(textButton).colspan(2);
        }
        
        t.row();
        textButton = new TextButton("Repack Texture Atlas", main.getSkin());
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                main.getDialogFactory().showDialogLoading(() -> {
                    try {
                        main.getProjectData().getAtlasData().writeAtlas();
                        main.getProjectData().getAtlasData().atlasCurrent = true;
                        main.getRootTable().produceAtlas();
                        main.getRootTable().refreshPreview();
                    } catch (Exception e) {
                        main.getDialogFactory().showDialogError("Error", "Unable to write texture atlas to temporary storage!", null);
                        Gdx.app.error(getClass().getName(), "Unable to write texture atlas to temporary storage!", e);
                        main.getDialogFactory().showDialogError("Atlas Error...", "Unable to write texture atlas to temporary storage.\n\nOpen log?");
                    }
                });
            }
        });
        textButton.addListener(main.getHandListener());
        t.add(textButton).colspan(2);
        
        t.row();
        label = new Label("Textures are rounded up to the next power of 2.", main.getSkin());
        t.add(label).colspan(2).padTop(10.0f);
        
        t.row();
        label = new Label("Max Texture Width: ", main.getSkin());
        t.add(label).right();
        Spinner spinner = new Spinner(main.getProjectData().getMaxTextureWidth(), 1.0, true, Orientation.HORIZONTAL, spinnerStyle);
        spinner.setMinimum(256.0);
        spinner.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                textureWidth = (int) spinner.getValue();
            }
        });
        spinner.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                textureWidth = (int) spinner.getValue();
            }
            
        });
        spinner.getTextField().addListener(main.getIbeamListener());
        spinner.getButtonMinus().addListener(main.getHandListener());
        spinner.getButtonPlus().addListener(main.getHandListener());
        t.add(spinner).minWidth(150.0f).left();
        
        t.row();
        label = new Label("Max Texture Height: ", main.getSkin());
        t.add(label).right();
        Spinner spinner2 = new Spinner(main.getProjectData().getMaxTextureHeight(), 1.0, true, Orientation.HORIZONTAL, spinnerStyle);
        spinner2.setMinimum(256.0);
        spinner2.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                textureHeight = (int) spinner2.getValue();
            }
        });
        spinner2.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                textureHeight = (int) spinner2.getValue();
            }
            
        });
        spinner2.getTextField().addListener(main.getIbeamListener());
        spinner2.getButtonMinus().addListener(main.getHandListener());
        spinner2.getButtonPlus().addListener(main.getHandListener());
        t.add(spinner2).minWidth(150.0f).left();
        
        t.row();
        ImageTextButton checkBox = new ImageTextButton("Strip whitespace on texture export", main.getSkin(), "checkbox");
        checkBox.setChecked(useStripWhitespace);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                useStripWhitespace = checkBox.isChecked();
            }
        });
        checkBox.addListener(main.getHandListener());
        t.add(checkBox).colspan(2).padTop(10.0f);
        
        t.row();
        label = new Label("Max Number of Undos: ", main.getSkin());
        t.add(label).right().padTop(10.0f);
        Spinner spinner3 = new Spinner(main.getProjectData().getMaxUndos(), 1.0, true, Orientation.HORIZONTAL, spinnerStyle);
        spinner3.setMinimum(1.0);
        spinner3.setMaximum(100.0);
        spinner3.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                maxUndos = (int) spinner3.getValue();
            }
        });
        spinner3.addListener(new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                maxUndos = (int) spinner3.getValue();
            }
            
        });
        spinner3.getTextField().addListener(main.getIbeamListener());
        spinner3.getButtonMinus().addListener(main.getHandListener());
        spinner3.getButtonPlus().addListener(main.getHandListener());
        t.add(spinner3).minWidth(150.0f).left().padTop(10.0f);
        
        spinner.setTransversalNext(spinner2.getTextField());
        spinner2.setTransversalNext(spinner3.getTextField());
        spinner3.setTransversalNext(spinner.getTextField());
        
        spinner.setTransversalPrevious(spinner3.getTextField());
        spinner2.setTransversalPrevious(spinner.getTextField());
        spinner3.setTransversalPrevious(spinner2.getTextField());
        
        button("OK", true);
        button ("Cancel", false);
        getButtonTable().getCells().first().getActor().addListener(main.getHandListener());
        getButtonTable().getCells().get(1).getActor().addListener(main.getHandListener());
        key(Keys.ESCAPE, false);
    }
}
