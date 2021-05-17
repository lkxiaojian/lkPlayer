//
// Created by root on 2021/5/17.
//

#include "LkFfmpage.h"


LkFfmpage::LkFfmpage(JavaCallHelper *javaCallHelper,  char *Path) {
    this->javaCallHelper=javaCallHelper;
    this->dataSource=Path;


}

LkFfmpage::~LkFfmpage() {

}

/**
 * 播放准备
 */
void LkFfmpage::prepare() {


}
