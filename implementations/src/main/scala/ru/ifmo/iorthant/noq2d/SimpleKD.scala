package ru.ifmo.iorthant.noq2d

import scala.reflect.ClassTag

import ru.ifmo.iorthant.util._

class SimpleKD[@specialized(Specialization.defaultSet) T](implicit m: Monoid[T])
  extends NoUpdateIncrementalOrthantSearch[T] {

  override type DataPointHandle = PlainArray.DataWrapper[T]
  override type QueryPointHandle = PlainArray.QueryWrapper[T]

  override implicit def dataPointHandleClassTag: ClassTag[DataPointHandle] = implicitly[ClassTag[DataPointHandle]]
  override implicit def queryPointHandleClassTag: ClassTag[QueryPointHandle] = implicitly[ClassTag[QueryPointHandle]]

  private var dataPoints: KDTree[DataPointHandle] = KDTree.empty
  private var queryPoints: KDTree[QueryPointHandle] = KDTree.empty

  override def addDataPoint(point: Array[Double], value: T): DataPointHandle = {
    val handle = new PlainArray.DataWrapper[T](point, value)
    dataPoints = dataPoints.add(point, handle)
    queryPoints.forDominating(new SimpleKD.AddContext[T](Dominance.negate(point), value))
    handle
  }

  override def addQueryPoint[I](point: Array[Double],
                                tracker: NoUpdateIncrementalOrthantSearch.UpdateTracker[T, I],
                                identifier: I): QueryPointHandle = {
    val value = makeQuery(point)
    val handle = new PlainArray.QueryWrapperImpl(Dominance.negate(point), value, tracker, identifier)
    queryPoints = queryPoints.add(handle.point, handle)
    handle
  }

  override def makeQuery(point: Array[Double]): T = {
    val context = new SimpleKD.QueryContext[T](point)
    dataPoints.forDominating(context)
    context.value
  }

  override def removeDataPoint(handle: DataPointHandle)
                              (implicit hm: HasMinus[T]): Unit = {
    dataPoints = dataPoints.remove(handle.point, handle)
    queryPoints.forDominating(new SimpleKD.RemoveContext[T](Dominance.negate(handle.point), handle.value))
  }

  override def removeQueryPoint(handle: QueryPointHandle): Unit = {
    queryPoints = queryPoints.remove(handle.point, handle)
  }
}

object SimpleKD {
  class QueryContext[@specialized(Specialization.defaultSet) T](val point: Array[Double])(implicit m: Monoid[T])
    extends KDTree.TraverseContext[PlainArray.DataWrapper[T]] {
    private var v = m.zero
    def value: T = v
    override def update(data: PlainArray.DataWrapper[T]): Unit = {
      v = m.plus(v, data.value)
    }
  }

  class AddContext[@specialized(Specialization.defaultSet) T](val point: Array[Double], val value: T)(implicit m: Monoid[T])
    extends KDTree.TraverseContext[PlainArray.QueryWrapper[T]] {
    override def update(data: PlainArray.QueryWrapper[T]): Unit = {
      data.plus(value)
    }
  }

  class RemoveContext[@specialized(Specialization.defaultSet) T](val point: Array[Double], val value: T)(implicit m: HasMinus[T])
    extends KDTree.TraverseContext[PlainArray.QueryWrapper[T]] {
    override def update(data: PlainArray.QueryWrapper[T]): Unit = {
      data.minus(value)
    }
  }
}
