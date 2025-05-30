package github.detrig.reminder.core


interface Navigation : LiveDataWrapper.Mutable<Screen> {

    class Base : Navigation, LiveDataWrapper.Abstract<Screen>()
}