// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gpp.ui.forms

import cats.implicits._
import cats.Show
import gem.util.Enumerated
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import react.common.ReactProps
import react.semanticui.modules.dropdown._
import scala.scalajs.js.JSConverters._
import react.semanticui.addons.select.Select
import scalajs.js
import gpp.ui._

/**
  * Produces a dropdown menu, similar to a combobox
  */
final case class EnumSelect[F[_], A](
  label:       String,
  value:       Option[A],
  placeholder: String,
  disabled:    Boolean,
  onChange:    js.UndefOr[A => F[Unit]] = js.undefined
)(
  implicit val run: RunCB[F],
  val enum:         Enumerated[A],
  val show:         Show[A]
) extends ReactProps {
  @inline def render: VdomElement =
    EnumSelect.component(this.asInstanceOf[EnumSelect[Any, Any]])
}

object EnumSelect {
  type Props[F[_], A] = EnumSelect[F, A]

  implicit protected def propsReuse[F[_], A]: Reusability[Props[F, A]] =
    Reusability.by(p => (p.label, p.value.map(p.show.show), p.placeholder, p.disabled))

  protected val component =
    ScalaComponent
      .builder[Props[Any, Any]]("EnumSelect")
      .stateless
      .render_P { p =>
        implicit val show = p.show

        <.div(
          ^.cls := "field",
          <.label(p.label),
          Select(
            placeholder = p.placeholder,
            fluid       = true,
            disabled    = p.disabled,
            value       = p.value.map(i => p.enum.tag(i)).orUndefined,
            options = p.enum.all
              .map(i => DropdownItem(text = i.show, value = p.enum.tag(i))),
            onChange = (ddp: Dropdown.DropdownProps) =>
              (for {
                onChange  <- p.onChange.toOption
                value     <- ddp.value.toOption
                enumValue <- p.enum.fromTag(value.asInstanceOf[String])
              } yield {
                p.run.runInCB(onChange(enumValue))
              }).getOrEmpty
          )
        )
      }
      .configure(Reusability.shouldComponentUpdate)
      .build
}
