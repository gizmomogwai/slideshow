/*
 * Copyright (c) 2019 E.S.R.Labs. All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of E.S.R.Labs and its suppliers, if any.
 * The intellectual and technical concepts contained herein are
 * proprietary to E.S.R.Labs and its suppliers and may be covered
 * by German and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from E.S.R.Labs.
 */
package com.flopcode.slideshow;

import com.flopcode.slideshow.database.Database;

import javax.swing.JLabel;
import java.awt.Color;

public class Statistics extends JLabel {

    public Statistics(Database database) throws Exception {
        setForeground(Color.red);
        setAlignmentX(0.0f);
        setAlignmentY(0.0f);
        database.addListener(db -> updateText(db));
    }

    private void updateText(Database database) {
        setText("total: " + database.getTotalCount() + "  selected: " + database.getGoodImageCount() + "  current: " + database.getCurrentIndex());
    }
}
