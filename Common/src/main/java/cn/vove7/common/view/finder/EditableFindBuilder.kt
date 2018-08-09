package cn.vove7.common.view.finder

/**
 * # EditableFindBuilder
 *
 * @author 17719
 * 2018/8/10
 */
class EditableFindBuilder : FindBuilder {
    constructor() {
        if (accessibilityService != null) {
            finder = EditableViewFinder(accessibilityService)
        }
    }


}