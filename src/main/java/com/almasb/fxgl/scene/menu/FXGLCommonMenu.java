/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015 AlmasB (almaslvl@gmail.com)
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
 */
package com.almasb.fxgl.scene.menu;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.event.MenuEvent;
import com.almasb.fxgl.scene.FXGLMenu;
import com.almasb.fxgl.ui.FXGLButton;
import com.almasb.fxgl.ui.UIFactory;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.function.Supplier;

/**
 * This is the default FXGL menu used if the users
 * don't provide their own. This class provides
 * common structures used in FXGL default menu style.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public abstract class FXGLCommonMenu extends FXGLMenu {

    private double menuX, menuY;

    public FXGLCommonMenu(GameApplication app) {
        super(app);


        MenuBox menu = createMenuBody();
        menuX = 50;
        menuY = app.getHeight() / 2 - menu.getLayoutHeight() / 2;

        // just a placeholder
        MenuBox menuContent = new MenuBox((int) app.getWidth() - 300 - 50);
        menuContent.setTranslateX(300);
        menuContent.setTranslateY(menu.getTranslateY());
        menuContent.setVisible(false);

        Title title = new Title(app.getSettings().getTitle());
        title.setTranslateX(app.getWidth() / 2 - title.getLayoutWidth() / 2);
        title.setTranslateY(menu.getTranslateY() / 2 - title.getLayoutHeight() / 2);

        Text version = UIFactory.newText("v" + app.getSettings().getVersion());
        version.setTranslateY(app.getHeight() - 2);

        getRoot().getChildren().addAll(createBackground(), title, version, menu, menuContent);

        app.getEventBus().addEventHandler(MenuEvent.PROFILE_SELECTED, event -> {
            String profileName = event.getData()
                    .map(o -> (String)o)
                    .orElseThrow(() -> new IllegalArgumentException(event + " has no data"));

            Text text = UIFactory.newText("Profile: " + profileName);
            text.setTranslateX(app.getWidth() - text.getLayoutBounds().getWidth());
            text.setTranslateY(app.getHeight() - 2);

            getRoot().getChildren().add(text);
        });
    }

    protected abstract MenuBox createMenuBody();

    /**
     *
     * @return background for menu
     */
    protected Node createBackground() {
        Rectangle bg = new Rectangle(app.getWidth(), app.getHeight());
        bg.setFill(Color.rgb(10, 1, 1));
        return bg;
    }

    protected MenuBox createOptionsMenu() {
        MenuItem itemControls = new MenuItem("CONTROLS");
        itemControls.setMenuContent(this::createContentControls);

        MenuItem itemVideo = new MenuItem("VIDEO");
        itemVideo.setMenuContent(this::createContentVideo);
        MenuItem itemAudio = new MenuItem("AUDIO");
        itemAudio.setMenuContent(this::createContentAudio);

        MenuItem btnRestore = new MenuItem("RESTORE");
        btnRestore.setOnAction(e -> {
            app.getDisplay().showConfirmationBox("Settings will be restored to default", yes -> {
                if (yes) app.loadFromDefaultProfile();
            });
        });

        return new MenuBox(200, itemControls, itemVideo, itemAudio, btnRestore);
    }

    protected MenuBox createExtraMenu() {
        MenuItem itemCredits = new MenuItem("CREDITS");
        itemCredits.setMenuContent(this::createContentCredits);

        MenuItem itemAchievements = new MenuItem("TROPHIES");
        itemAchievements.setMenuContent(this::createContentAchievements);

        return new MenuBox(200, itemCredits, itemAchievements);
    }

    private void switchMenuTo(MenuBox menu) {
        Node oldMenu = getRoot().getChildren().get(3);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.33), oldMenu);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            menu.setTranslateX(menuX);
            menu.setTranslateY(menuY);
            menu.setOpacity(0);
            getRoot().getChildren().set(3, menu);
            oldMenu.setOpacity(1);

            FadeTransition ft2 = new FadeTransition(Duration.seconds(0.33), menu);
            ft2.setToValue(1);
            ft2.play();
        });
        ft.play();
    }

    private void switchMenuContentTo(MenuContent content) {
        content.setTranslateX(menuX * 2 + 200);
        content.setTranslateY(menuY);
        getRoot().getChildren().set(4, content);
    }

    protected static class Title extends StackPane {
        private Text text;

        public Title(String name) {
            text = UIFactory.newText(name, 50);

            Rectangle bg = new Rectangle(text.getLayoutBounds().getWidth() + 20, 60);
            bg.setStroke(Color.WHITE);
            bg.setStrokeWidth(2);
            bg.setFill(null);

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);
        }

        public double getLayoutWidth() {
            return text.getLayoutBounds().getWidth() + 20;
        }

        public double getLayoutHeight() {
            return text.getLayoutBounds().getHeight() + 20;
        }
    }

    protected static class MenuBox extends VBox {
        public MenuBox(int width, MenuItem... items) {
            getChildren().add(createSeparator(width));

            for (MenuItem item : items) {
                item.setParent(this);
                getChildren().addAll(item, createSeparator(width));
            }
        }

        private Line createSeparator(int width) {
            Line sep = new Line();
            sep.setEndX(width);
            sep.setStroke(Color.DARKGREY);
            return sep;
        }

        public double getLayoutHeight() {
            return 10 * getChildren().size();
        }
    }

    protected class MenuItem extends FXGLButton {
        private MenuBox parent;
        private MenuContent content;

        public MenuItem(String name) {
            super(name);
        }

        public void setParent(MenuBox menu) {
            parent = menu;
        }

        public void setMenuContent(Supplier<MenuContent> contentSupplier) {
            this.addEventHandler(ActionEvent.ACTION, event -> {
//                if (content == null) {
//                    content = contentSupplier.get();
//                }

                switchMenuContentTo(contentSupplier.get());
            });
        }

        public void setChild(MenuBox menu) {
            MenuItem back = new MenuItem("BACK");
            menu.getChildren().add(0, back);

            back.addEventHandler(ActionEvent.ACTION, event -> {
                switchMenuTo(MenuItem.this.parent);
            });

            this.addEventHandler(ActionEvent.ACTION, event -> {
                switchMenuTo(menu);
            });
        }
    }
}
