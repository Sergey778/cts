package util.templates

import com.twitter.finatra.response.Mustache

@Mustache("hierarchy_list_element")
case class HierarchyListElement(name: String, childs: List[HierarchyListElement]) {
  val empty = childs.isEmpty
}
