package opcon.com.printer

import android.util.EventLog
import java.lang.reflect.Type
import kotlin.properties.Delegates

/**
 * Created by aslitaskiran on 14/10/2017.
 */

interface EventListener<in Type> {
    fun onEvent(obj: Type)
}

class ObservableObject<Type>(initialValue: Type) {
    var listener: EventListener<Type>? = null
    var obj: Type by Delegates.observable(
            initialValue = initialValue,
            onChange = {
                _, _, new ->
                listener?.onEvent(new)
            }
    )
    fun onEvent(callback: (new: Type) -> Unit) {
        listener = object : EventListener<Type> {
            override fun onEvent(obj: Type) {
                callback(obj)
            }
        }
    }
}

class EventManager {

    var observableObjects: HashMap<String, ObservableObject<*>> = HashMap()

    fun <Type>on(event: String, initialValue:Type, callback: (new: Type) -> Unit) {
        throwEventIsAlreadyExists(event)
        val observable = ObservableObject(initialValue)
        observable.onEvent { callback(it) }
        observableObjects.put(event, observable)
    }

    fun throwEventIsAlreadyExists(event: String){
        observableObjects.mapKeys {
            if (it.key == event)
                throw IllegalStateException("$event already exists in the EventManager!")
        }
    }

    fun non(event: String) {
        observableObjects.remove(event)
    }

    fun <Type> dispatch(event: String, new: Type) {
        // ignore unchecked cast warring.
        (observableObjects[event] as ObservableObject<Type>).obj = new
    }

    fun release() {
        observableObjects.clear()
    }

}