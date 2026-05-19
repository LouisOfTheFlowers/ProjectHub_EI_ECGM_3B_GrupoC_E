-- Allow admins to manage user profiles from the team management screen.

drop policy if exists "Admins can update users" on public.users;
drop policy if exists "Admins can delete users" on public.users;

create policy "Admins can update users"
on public.users
for update
to authenticated
using (public.current_user_is_admin())
with check (public.current_user_is_admin());

create policy "Admins can delete users"
on public.users
for delete
to authenticated
using (public.current_user_is_admin());

create or replace function public.admin_delete_user(p_user_id integer)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  target_email text;
begin
  if not public.current_user_is_admin() then
    raise exception 'Apenas administradores podem remover utilizadores.';
  end if;

  select email
  into target_email
  from public.users
  where id = p_user_id;

  if target_email is null then
    raise exception 'Utilizador não encontrado.';
  end if;

  if target_email = auth.jwt() ->> 'email' then
    raise exception 'Não podes remover a tua própria conta de administrador.';
  end if;

  delete from public.observacao_fotos
  where observacao_id in (
    select observacoes.id
    from public.observacoes
    inner join public.registos_tarefa
      on registos_tarefa.id = observacoes.registo_id
    where registos_tarefa.user_id = p_user_id
  );

  delete from public.observacoes
  where registo_id in (
    select id
    from public.registos_tarefa
    where user_id = p_user_id
  );

  delete from public.registos_tarefa
  where user_id = p_user_id;

  delete from public.avaliacoes
  where user_id = p_user_id;

  delete from public.tarefa_users
  where user_id = p_user_id;

  delete from public.projeto_users
  where user_id = p_user_id;

  update public.projetos
  set gestor_id = null
  where gestor_id = p_user_id;

  delete from public.users
  where id = p_user_id;

  delete from auth.users
  where email = target_email;
end;
$$;

grant execute on function public.admin_delete_user(integer) to authenticated;
