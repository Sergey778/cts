package util.templates

import com.twitter.finatra.response.Mustache

@Mustache("hierarchy_list")
trait HierarchyList{
  val list: List[HierarchyListElement]
}
