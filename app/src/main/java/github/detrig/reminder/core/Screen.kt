package github.detrig.corporatekanbanboard.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

interface Screen {

    fun show(supportFragmentManager: FragmentManager, containerId: Int)

    abstract class Replace(private val fragmentClass : Class<out Fragment>) : Screen {

        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {
            supportFragmentManager
                .beginTransaction()
                .addToBackStack(fragmentClass.name)
                .replace(containerId, fragmentClass.getDeclaredConstructor().newInstance())
                .commit()
        }
    }

    abstract class Add(private val fragmentClass : Class<out Fragment>) : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {
            supportFragmentManager
                .beginTransaction()
                .add(containerId, fragmentClass.getDeclaredConstructor().newInstance())
                .addToBackStack(fragmentClass.name)
                .commit()
        }
    }

    abstract class ReplaceMain(private val fragmentClass: Class<out Fragment>) : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {

            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            supportFragmentManager
                .beginTransaction()
                .replace(containerId, fragmentClass.getDeclaredConstructor().newInstance())
                .commit()
        }
    }

    object Pop : Screen {
        override fun show(supportFragmentManager: FragmentManager, containerId: Int) {
            supportFragmentManager.popBackStack()
        }
    }
}