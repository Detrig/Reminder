package github.detrig.corporatekanbanboard.core


interface Navigation : LiveDataWrapper.Mutable<Screen> {

    class Base : Navigation, LiveDataWrapper.Abstract<Screen>()
}