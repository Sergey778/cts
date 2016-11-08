package util.templates

import com.twitter.finatra.response.Mustache

@Mustache("hierarchy_select_element")
case class HierarchySelectElement(value: String, name: String, childs: List[HierarchySelectElement], margin: Int = 0) {
  val empty = childs.isEmpty

  val tab = (0 until margin).map(x => '-').mkString
}
