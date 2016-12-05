package util

import com.typesafe.config.Config

object Paths extends ConfigHolder {

  protected override final val configuration: Config = rootConfiguration.getConfig("paths")

  implicit class PathExtension(val path: String) extends AnyVal {
    def wildcard(wildcardName: String = "*") = s"$path/:$wildcardName"
    def element(elementName: String) = s"$path/$elementName"
    def api = s"/api$path"
  }

  final val domain: String = config("domain.url") getOrElse "localhost:8888"

  final val signIn: String = config("auth.signIn") getOrElse "/signin"
  final val signUp: String = config("auth.signUp") getOrElse "/signup"
  final val signUpConfirmation: String = config("auth.signUpConfirm") getOrElse "/signupconfirm"
  final val signOut: String = config("auth.signOut") getOrElse "/signout"
  final val signUpConfirmInfo: String = config("auth.signUpConfirmInfo") getOrElse "/signupconfirminfo"

  final val profile: String = config("profile.index") getOrElse "/profile"
  final val profileQuestions: String = config("profile.questions") getOrElse "/profile/questions"
  final val profileQuestionCreate: String = config("profile.questions.create") getOrElse "/profile/questions/create"
  final val profileAnswers: String = config("profile.answers") getOrElse "/profile/answers"
  final val profileAnswersAdd: String = config("profile.answers.add") getOrElse "/profile/answers/add"

  final val profileQuestionGroups: String = config("profile.questionGroups") getOrElse "/profile/question-groups"
  final val profileQuestionGroupsCreate: String =
    config("profile.questionGroups.create") getOrElse "/profile/question-groups/create"

  final val userGroups: String = config("usergroups.index") getOrElse "/user-groups"
  final val userGroupsAll: String = config("usergroups.all") getOrElse "/user-groups/all"
  final val userGroupsMy: String = config("usergroups.my") getOrElse "/user-groups/my"
  final val userGroupsCreate: String = config("usergroups.create") getOrElse "/user-groups/create"
  final val userGroupsAcceptInvite: String = config("usergroups.invite.accept") getOrElse "/user-groups/invite-accept"
  final val userGroupsInviteSend: String = config("usergroups.invite.send") getOrElse "/user-groups/invite"
  final val userGroupsInviteApply: String = config("usergroups.invite.apply") getOrElse "/user-groups/apply-group"
  final val userGroupsInviteUser: String = config("usergroups.invite.senduser") getOrElse "/user-groups/invite-user"

  final val courses: String = config("courses.index") getOrElse "/courses"
  final val coursesAll: String = config("courses.all") getOrElse "/courses/all"
  final val coursesCreate: String = config("courses.create") getOrElse "/courses/create"
  final val coursesAddUserGroup: String = config("courses.usergroups.add") getOrElse "/courses/user-groups/add"
  final val coursesAddTest: String = config("courses.tests.add") getOrElse "/courses/tests/add"
}
