/*
 * Copyright (C) 2015 johnlindsay
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package whitebox.ui;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

/**
 *
 * @author johnlindsay
 */
public class ScrollerProperty extends JComponent implements MouseListener,
        PropertyChangeListener {
    
    private String labelText;
    private double value = -1;
    private Color backColour = Color.WHITE;
    private int leftMargin = 10;
    private int rightMargin = 10;
    private int preferredWidth = 200;
    private int preferredHeight = 24;
    private Boolean parseIntegersOnly = false;
    private double minValue = 0d;
    private double maxValue = 100d;
    private int scrollerRange = 100;
    private JScrollBar scrollBar = new JScrollBar();
    //private NumberFormat numberFormat;
    private String lowerLabel = "low";
    private String upperLabel = "high";

    public ScrollerProperty() {
        setOpaque(true);
        revalidate();
    }

//    public ScrollerProperty(String labelText, double value) {
//        setOpaque(true);
//        this.labelText = labelText;
//        this.value = value;
//        revalidate();
//    }

    public ScrollerProperty(String labelText, double value, double minValue, double maxValue, int scrollerRange) {
        setOpaque(true);
        this.labelText = labelText;
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.scrollerRange = scrollerRange;
        revalidate();
    }

    public Color getBackColour() {
        return backColour;
    }

    public void setBackColour(Color backColour) {
        this.backColour = backColour;
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        double oldValue = this.value;
        this.value = value;
        firePropertyChange("value", oldValue, value);
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    
    public Boolean getParseIntegersOnly() {
        return parseIntegersOnly;
    }

    public void setParseIntegersOnly(Boolean integerNumbersOnly) {
        this.parseIntegersOnly = integerNumbersOnly;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }
    
    public String getLowerLabel() {
        return lowerLabel;
    }
    
    public void setLowerLabel(String label) {
        this.lowerLabel = label;
    }
    
    public String getUpperLabel() {
        return upperLabel;
    }

    public void setUpperLabel(String label) {
        this.upperLabel = label;
    }

    @Override
    public final void revalidate() {
        this.removeAll();
        //this.setupFormat();

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBackground(backColour);
        this.add(Box.createHorizontalStrut(leftMargin));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.add(label);
        this.add(Box.createHorizontalGlue());
        this.add(new JLabel(lowerLabel));
        this.add(Box.createHorizontalStrut(5));
        if (this.value < this.minValue) { this.value = this.minValue; }
        if (this.value > this.maxValue) { this.value = this.maxValue; }
        int intVal = (int)(this.scrollerRange * (this.value - this.minValue) / (this.maxValue - this.minValue));
        scrollBar = new JScrollBar(Adjustable.HORIZONTAL, intVal, 0, 0, this.scrollerRange);
        //formattedTextField.setMaximumSize(new Dimension(5000, 24));
        scrollBar.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                scrollBar.getPreferredSize().height));
        scrollBar.addPropertyChangeListener("value", this);
        scrollBar.addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                double scrollerVal = minValue + ((double)scrollBar.getValue() / scrollerRange) * (maxValue - minValue);
                setValue(scrollerVal);
            }
        });
        this.add(scrollBar);
        this.add(Box.createHorizontalStrut(5));
        this.add(new JLabel(upperLabel));
        //formattedTextField.revalidate();
        this.add(Box.createHorizontalStrut(rightMargin));
        super.revalidate();
    }

//    private void setupFormat() {
//        numberFormat = NumberFormat.getNumberInstance();
//        if (parseIntegersOnly) {
//            numberFormat.setParseIntegerOnly(true);
//        }
//    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        try {
            Object source = e.getSource();
            if (source == scrollBar) {
                double scrollerVal = this.minValue + ((double)scrollBar.getValue() / this.scrollerRange) * (this.maxValue - this.minValue);
                if (parseIntegersOnly) {
                    scrollerVal = Math.round(scrollerVal);
                }
                if (scrollerVal < this.minValue) { scrollerVal = this.minValue; }
                if (scrollerVal > this.maxValue) { scrollerVal = this.maxValue; }
                setValue(scrollerVal);
//                if (parseIntegersOnly) {
//                    int val = ((Number) scrollBar.getValue()).intValue();
//                    if (val < minValue) {
//                        val = (int) minValue;
//                        scrollBar.setValue(val);
//                    }
//                    if (val > maxValue) {
//                        val = (int) maxValue;
//                        scrollBar.setValue(val);
//                    }
//                    setValue(val);
//                } else {
//                    double val = ((Number) scrollBar.getValue()).doubleValue();
//                    if (val < minValue) {
//                        val = minValue;
//                        scrollBar.setValue((int)val);
//                    }
//                    if (val > maxValue) {
//                        val = maxValue;
//                        scrollBar.setValue((int)val);
//                    }
//                    setValue(val);
//                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
