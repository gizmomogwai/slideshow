import sys
from PyQt5 import QtCore, QtWidgets
from PyQt5.QtWidgets import QMainWindow, QLabel, QGridLayout, QWidget
from PyQt5.QtCore import Qt, QSize
from PyQt5.QtGui import QPixmap
from PyQt5.QtCore import QRect, QPropertyAnimation, QParallelAnimationGroup, QEasingCurve

from functools import reduce


class Images:
    def __init__(self):
        self.images = ["1.jpg", "2.jpg"]
        self.index = 0
    def next(self):
        res = self.images[self.index]
        self.index += 1
        if self.index >= len(self.images):
            self.index = 0
        return res

class Row:
    def __init__(self, images, slideshow, width, height):
        self.images = images
        self.slideshow = slideshow
        self.width = width
        self.height = height
        self.row = []

    def prepare(self):
        if (len(self.row) > 0):
            del self.row[-1]
        while self.current_row_width() < self.width:
            next_image = self.images.next()
            image = QPixmap(next_image).scaledToHeight(self.height, Qt.SmoothTransformation)
            self.row = [image] + self.row
            
        return self.row
    
    def current_row_width(self):
        return reduce(lambda sum, x: sum + x.width(), self.row, 0)
        
        
class Slideshow(QMainWindow):
    def create_labels(self, images):
        for label in self.labels:
            label.setParent(None)

        x = 0
        for image in images:
            label = QLabel(self)
            label.setPixmap(image)
            label.setParent(self)
            label.setGeometry(QRect(x, 0, image.width(), image.height()))
            x = x + image.width()
            label.resize(image.width(), image.height())
            self.labels.append(label)
            
    def __init__(self, images):
        QMainWindow.__init__(self)
        self.images = images
        self.setMinimumSize(QSize(640, 480))    
        self.setWindowTitle("Hello world") 
        self.row = Row(images, self, 320*4, 240)
        self.labels = []

        images = self.row.prepare()
        self.labels = self.create_labels(images)

#        anim = QParallelAnimationGroup()
#        a1 = QPropertyAnimation(t1, b"geometry")
#        a1.setDuration(1000)
#        a1.setStartValue(QRect(-640, 0, 640, 480))
#        a1.setEndValue(QRect(0, 0, 640, 480))
#        ease = QEasingCurve.InOutQuad # https://doc.qt.io/qt-5/qeasingcurve.html#Type-enum
#        #ease = QEasingCurve.InOutCirc
#        #ease = QEasingCurve.InOutElastic
#        #ease = QEasingCurve.InOutBounce
#        #QEasingCurve.InOutQuad
#        #ease = QEasingCurve.OutBack
#        #QEasingCurve.OutBounce
#        a1.setEasingCurve(ease)
#        anim.addAnimation(a1)
#
#    
#        a2 = QPropertyAnimation(t2, b"geometry")
#        a2.setDuration(1000)
#        a2.setStartValue(QRect(0, 0, 640, 480))
#        a2.setEndValue(QRect(640, 0, 640, 480))
#        a2.setEasingCurve(ease)
#        anim.addAnimation(a2)
#
#        self.anim = anim
#        anim.finished.connect(self.animation_finished)
#        anim.start()
    def animation_finished(self):
        print("done")
        
if __name__ == "__main__":
    app = QtWidgets.QApplication(sys.argv)
    images = Images()
    mainWin = Slideshow(images)
    mainWin.show()
    sys.exit( app.exec_() )
